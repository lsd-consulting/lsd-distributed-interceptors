package io.lsdconsulting.lsd.distributed.interceptor.integration

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.github.tomakehurst.wiremock.client.WireMock
import com.lsdconsulting.generatorui.controller.LsdControllerStub
import io.lsdconsulting.lsd.distributed.access.model.InteractionType
import io.lsdconsulting.lsd.distributed.access.model.InteractionType.*
import io.lsdconsulting.lsd.distributed.access.model.InterceptedInteraction
import io.lsdconsulting.lsd.distributed.interceptor.integration.data.TraceIdGenerator
import io.lsdconsulting.lsd.distributed.interceptor.integration.testapp.controller.event.SomethingDoneEvent
import org.apache.commons.lang3.RandomStringUtils
import org.awaitility.Awaitility
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.notNullValue
import org.jeasy.random.EasyRandom
import org.jeasy.random.EasyRandomParameters
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpStatus
import java.net.URISyntaxException
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.*


class InteractionHttpRecordingIT : IntegrationTestBase() {
    @Autowired
    private lateinit var rabbitTemplate: RabbitTemplate
    private val mainTraceId = TraceIdGenerator.generate()
    private val sourceName = RandomStringUtils.randomAlphanumeric(10).uppercase(Locale.getDefault())
    private val targetName = RandomStringUtils.randomAlphanumeric(10).uppercase(Locale.getDefault())
    private val mapper = ObjectMapper()
    private val lsdControllerStub = LsdControllerStub(mapper)

    private val easyRandom: EasyRandom = EasyRandom(EasyRandomParameters().seed(System.currentTimeMillis()))

    @BeforeEach
    fun setup() {
        WireMock.reset()
        mapper.registerModule(KotlinModule.Builder().build())
        mapper.registerModule(JavaTimeModule())
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        lsdControllerStub.store(easyRandom.nextObject(InterceptedInteraction::class.java))
    }

    @Test
    @DisplayName("Should record interactions from RestTemplate, FeignClient and RabbitListener")
    @Throws(
        URISyntaxException::class
    )
    fun shouldRecordRestTemplateFeignClientAndListenerInteractions() {
        givenExternalApi()
        val response = sentRequest("/api-listener", mainTraceId, null, null)
        assertThat(response.statusCode, `is`(HttpStatus.OK))
        assertThat(response.body, `is`("response_from_controller"))
        Awaitility.await().untilAsserted {

            // Assert calls to http storage endpoint
            verify(
                buildExpectedInterceptedInteraction(
                    NO_BODY,
                    "TestApp",
                    "/api-listener?message=from_test",
                    "/api-listener?message=from_test",
                    null,
                    "GET",
                    REQUEST
                )
            )
            verify(
                buildExpectedInterceptedInteraction(
                    "response_from_controller",
                    "TestApp",
                    "/api-listener?message=from_test",
                    "/api-listener?message=from_test",
                    "200 OK",
                    null,
                    RESPONSE
                )
            )
            verify(
                buildExpectedInterceptedInteraction(
                    "{\"message\":\"from_controller\"}",
                    "TestApp",
                    "SomethingDoneEvent",
                    "SomethingDoneEvent",
                    null,
                    null,
                    PUBLISH
                )
            )
            verify(
                buildExpectedInterceptedInteraction(
                    "{\"message\":\"from_controller\"}",
                    "TestApp",
                    "SomethingDoneEvent",
                    "SomethingDoneEvent",
                    null,
                    null,
                    CONSUME
                )
            )
            verify(
                buildExpectedInterceptedInteraction(
                    "from_listener",
                    "TestApp",
                    "UNKNOWN_TARGET",
                    "/external-api?message=from_feign",
                    null,
                    "POST",
                    REQUEST
                )
            )
            verify(
                buildExpectedInterceptedInteraction(
                    "from_external",
                    "TestApp",
                    "UNKNOWN_TARGET",
                    "/external-api?message=from_feign",
                    "200 OK",
                    null,
                    RESPONSE
                )
            )
            verify(
                buildExpectedInterceptedInteraction(
                    "from_listener",
                    "TestApp",
                    "Downstream",
                    "/external-api?message=from_feign",
                    null,
                    "POST",
                    REQUEST
                )
            )
            verify(
                buildExpectedInterceptedInteraction(
                    "from_external",
                    "TestApp",
                    "Downstream",
                    "/external-api?message=from_feign",
                    "200 OK",
                    null,
                    RESPONSE
                )
            )
        }
    }

    @Test
    @DisplayName("Should record interactions with supplied names through headers")
    @Throws(
        URISyntaxException::class
    )
    fun shouldRecordHeaderSuppliedNames() {
        givenExternalApi()
        val response = sentRequest("/api-listener", mainTraceId, sourceName, targetName)
        assertThat(response.statusCode, `is`(HttpStatus.OK))
        assertThat(response.body, Matchers.containsString("response_from_controller"))
        Awaitility.await().untilAsserted {

            // Assert calls to http storage endpoint
            verify(
                buildExpectedInterceptedInteraction(
                    NO_BODY,
                    sourceName,
                    targetName,
                    "/api-listener?message=from_test",
                    null,
                    "GET",
                    REQUEST
                )
            )
            verify(
                buildExpectedInterceptedInteraction(
                    "response_from_controller",
                    sourceName,
                    targetName,
                    "/api-listener?message=from_test",
                    "200 OK",
                    null,
                    RESPONSE
                )
            )
            verify(
                buildExpectedInterceptedInteraction(
                    "{\"message\":\"from_controller\"}",
                    "TestApp",
                    "SomethingDoneEvent",
                    "SomethingDoneEvent",
                    null,
                    null,
                    PUBLISH
                )
            )
            verify(
                buildExpectedInterceptedInteraction(
                    "{\"message\":\"from_controller\"}",
                    "TestApp",
                    "SomethingDoneEvent",
                    "SomethingDoneEvent",
                    null,
                    null,
                    CONSUME
                )
            )
            verify(
                buildExpectedInterceptedInteraction(
                    "from_listener",
                    "TestApp",
                    "UNKNOWN_TARGET",
                    "/external-api?message=from_feign",
                    null,
                    "POST",
                    REQUEST
                )
            )
            verify(
                buildExpectedInterceptedInteraction(
                    "from_external",
                    "TestApp",
                    "UNKNOWN_TARGET",
                    "/external-api?message=from_feign",
                    "200 OK",
                    null,
                    RESPONSE
                )
            )
            verify(
                buildExpectedInterceptedInteraction(
                    "from_listener",
                    "TestApp",
                    "Downstream",
                    "/external-api?message=from_feign",
                    null,
                    "POST",
                    REQUEST
                )
            )
            verify(
                buildExpectedInterceptedInteraction(
                    "from_external",
                    "TestApp",
                    "Downstream",
                    "/external-api?message=from_feign",
                    "200 OK",
                    null,
                    RESPONSE
                )
            )
        }
    }

    @Test
    @Throws(URISyntaxException::class)
    fun shouldRecordReceivingMessagesWithRabbitTemplate() {
        val response = sentRequest("/api-rabbit-template", mainTraceId, sourceName, targetName)
        assertThat(response.statusCode, `is`(HttpStatus.OK))
        assertThat(response.body, `is`("response_from_controller"))
        val type: ParameterizedTypeReference<SomethingDoneEvent> = object : ParameterizedTypeReference<SomethingDoneEvent>() {}
        Awaitility.await().untilAsserted {
            val message = rabbitTemplate.receiveAndConvert("queue-rabbit-template", 2000, type)
            assertThat(message, `is`(notNullValue()))
            assertThat(message?.message, `is`("from_controller"))
        }
        Awaitility.await().untilAsserted {

            // Assert calls to http storage endpoint
            verify(
                buildExpectedInterceptedInteraction(
                    NO_BODY,
                    sourceName,
                    targetName,
                    "/api-rabbit-template?message=from_test",
                    null,
                    "GET",
                    REQUEST
                )
            )
            verify(
                buildExpectedInterceptedInteraction(
                    "response_from_controller",
                    sourceName,
                    targetName,
                    "/api-rabbit-template?message=from_test",
                    "200 OK",
                    null,
                    RESPONSE
                )
            )
            verify(
                buildExpectedInterceptedInteraction(
                    "{\"message\":\"from_controller\"}",
                    "TestApp",
                    "SomethingDoneEvent",
                    "SomethingDoneEvent",
                    null,
                    null,
                    PUBLISH
                )
            )
            verify(
                buildExpectedInterceptedInteraction(
                    "{\"message\":\"from_controller\"}",
                    "TestApp",
                    "SomethingDoneEvent",
                    "SomethingDoneEvent",
                    null,
                    null,
                    CONSUME
                )
            )
        }
    }

    @Test
    @Throws(URISyntaxException::class)
    fun shouldRecordObfuscatedHeaders() {
        givenExternalApi()
        val response = sentRequest("/api-listener", mainTraceId, null, null)
        assertThat(response.statusCode, `is`(HttpStatus.OK))
        Awaitility.await().untilAsserted {
            val requestPatternBuilder = WireMock.postRequestedFor(WireMock.urlPathEqualTo("/lsds"))
            requestPatternBuilder.withRequestBody(
                WireMock.matchingJsonPath(
                    "$.requestHeaders['Authorization']",
                    WireMock.notContaining("Password")
                )
            )
            WireMock.verify(requestPatternBuilder)
        }
    }

    private fun buildExpectedInterceptedInteraction(
        body: String,
        serviceName: String,
        target: String,
        path: String,
        httpStatus: String?,
        httpMethod: String?,
        interactionType: InteractionType
    ): InterceptedInteraction {
        return InterceptedInteraction(
            traceId = mainTraceId,
            body = body,
            serviceName = serviceName,
            target = target,
            path = path,
            httpStatus = httpStatus,
            httpMethod = httpMethod,
            interactionType = interactionType,
            profile = "",
            elapsedTime = 0L,
            createdAt = ZonedDateTime.now(ZoneId.of("UTC"))
        )
    }

    companion object {
        private const val NO_BODY = ""
        private fun verify(interceptedInteraction: InterceptedInteraction) {
            val requestPatternBuilder = WireMock.postRequestedFor(WireMock.urlPathEqualTo("/lsds"))
            requestPatternBuilder.withRequestBody(
                WireMock.matchingJsonPath(
                    "$.traceId",
                    WireMock.equalTo(interceptedInteraction.traceId)
                )
            )
            requestPatternBuilder.withRequestBody(
                WireMock.matchingJsonPath(
                    "$.body",
                    WireMock.equalTo(interceptedInteraction.body)
                )
            )
            requestPatternBuilder.withRequestBody(
                WireMock.matchingJsonPath(
                    "$.serviceName",
                    WireMock.equalTo(interceptedInteraction.serviceName)
                )
            )
            requestPatternBuilder.withRequestBody(
                WireMock.matchingJsonPath(
                    "$.target",
                    WireMock.equalTo(interceptedInteraction.target)
                )
            )
            requestPatternBuilder.withRequestBody(
                WireMock.matchingJsonPath(
                    "$.path",
                    WireMock.equalTo(interceptedInteraction.path)
                )
            )
            if (interceptedInteraction.httpStatus != null) {
                requestPatternBuilder.withRequestBody(
                    WireMock.matchingJsonPath(
                        "$.httpStatus",
                        WireMock.equalTo(interceptedInteraction.httpStatus)
                    )
                )
            } else {
                requestPatternBuilder.withRequestBody(WireMock.matchingJsonPath("$.httpStatus", WireMock.absent()))
            }
            if (interceptedInteraction.httpMethod != null) {
                requestPatternBuilder.withRequestBody(
                    WireMock.matchingJsonPath(
                        "$.httpMethod",
                        WireMock.equalTo(interceptedInteraction.httpMethod)
                    )
                )
            } else {
                requestPatternBuilder.withRequestBody(WireMock.matchingJsonPath("$.httpMethod", WireMock.absent()))
            }
            requestPatternBuilder.withRequestBody(
                WireMock.matchingJsonPath(
                    "$.interactionType",
                    WireMock.equalTo(interceptedInteraction.interactionType.name)
                )
            )
            requestPatternBuilder.withRequestBody(
                WireMock.matchingJsonPath(
                    "$.profile",
                    WireMock.equalTo(interceptedInteraction.profile)
                )
            )
            WireMock.verify(requestPatternBuilder)
        }
    }
}