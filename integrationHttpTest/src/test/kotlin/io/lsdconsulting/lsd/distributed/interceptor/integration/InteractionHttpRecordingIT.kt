package io.lsdconsulting.lsd.distributed.interceptor.integration

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.github.tomakehurst.wiremock.client.WireMock.*
import com.lsdconsulting.generatorui.controller.LsdControllerStub
import io.github.krandom.KRandom
import io.github.krandom.KRandomParameters
import io.lsdconsulting.lsd.distributed.connector.model.InteractionType
import io.lsdconsulting.lsd.distributed.connector.model.InteractionType.*
import io.lsdconsulting.lsd.distributed.connector.model.InterceptedInteraction
import io.lsdconsulting.lsd.distributed.interceptor.integration.data.TraceIdGenerator
import io.lsdconsulting.lsd.distributed.interceptor.integration.testapp.controller.event.SomethingDoneEvent
import org.apache.commons.lang3.RandomStringUtils.secure
import org.awaitility.Awaitility.await
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.notNullValue
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
    private val sourceName = secure().nextAlphanumeric(10).uppercase(Locale.getDefault())
    private val targetName = secure().nextAlphanumeric(10).uppercase(Locale.getDefault())
    private val mapper = ObjectMapper()
    private val lsdControllerStub = LsdControllerStub(mapper)

    private val kRandom: KRandom = KRandom(KRandomParameters().seed(System.currentTimeMillis()))

    @BeforeEach
    fun setup() {
        reset()
        mapper.registerModule(KotlinModule.Builder().build())
        mapper.registerModule(JavaTimeModule())
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        lsdControllerStub.store(kRandom.nextObject(InterceptedInteraction::class.java))
    }

    @Test
    @DisplayName("Should record interactions from RestTemplate, FeignClient and RabbitListener")
    @Throws(URISyntaxException::class)
    fun `should record rest template feign client and listener interactions`() {
        givenExternalApi()
        val response = sentRequest("/api-listener", mainTraceId, null, null)
        assertThat(response.statusCode, `is`(HttpStatus.OK))
        assertThat(response.body, `is`("response_from_controller"))
        await().untilAsserted {

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
    fun `should record header supplied names`() {
        givenExternalApi()
        val response = sentRequest("/api-listener", mainTraceId, sourceName, targetName)
        assertThat(response.statusCode, `is`(HttpStatus.OK))
        assertThat(response.body, Matchers.containsString("response_from_controller"))
        await().untilAsserted {

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
    fun `should record receiving messages with rabbit template`() {
        val response = sentRequest("/api-rabbit-template", mainTraceId, sourceName, targetName)
        assertThat(response.statusCode, `is`(HttpStatus.OK))
        assertThat(response.body, `is`("response_from_controller"))
        val type: ParameterizedTypeReference<SomethingDoneEvent> = object : ParameterizedTypeReference<SomethingDoneEvent>() {}
        await().untilAsserted {
            val message = rabbitTemplate.receiveAndConvert("queue-rabbit-template", 2000, type)
            assertThat(message, `is`(notNullValue()))
            assertThat(message?.message, `is`("from_controller"))
        }
        await().untilAsserted {

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
    fun `should record obfuscated headers`() {
        givenExternalApi()
        val response = sentRequest("/api-listener", mainTraceId, null, null)
        assertThat(response.statusCode, `is`(HttpStatus.OK))
        await().untilAsserted {
            val requestPatternBuilder = postRequestedFor(urlPathEqualTo("/lsds"))
            requestPatternBuilder.withRequestBody(
                matchingJsonPath(
                    "$.requestHeaders['Authorization']",
                    notContaining("Password")
                )
            )
            verify(requestPatternBuilder)
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
            val requestPatternBuilder = postRequestedFor(urlPathEqualTo("/lsds"))
            requestPatternBuilder.withRequestBody(matchingJsonPath("$.traceId", equalTo(interceptedInteraction.traceId)))
            requestPatternBuilder.withRequestBody(matchingJsonPath("$.body", equalTo(interceptedInteraction.body)))
            requestPatternBuilder.withRequestBody(matchingJsonPath("$.serviceName", equalTo(interceptedInteraction.serviceName)))
            requestPatternBuilder.withRequestBody(matchingJsonPath("$.target", equalTo(interceptedInteraction.target)))
            requestPatternBuilder.withRequestBody(matchingJsonPath("$.path", equalTo(interceptedInteraction.path)))
            if (interceptedInteraction.httpStatus != null) {
                requestPatternBuilder.withRequestBody(matchingJsonPath("$.httpStatus", equalTo(interceptedInteraction.httpStatus)))
            } else {
                requestPatternBuilder.withRequestBody(matchingJsonPath("$.httpStatus", absent()))
            }
            if (interceptedInteraction.httpMethod != null) {
                requestPatternBuilder.withRequestBody(matchingJsonPath("$.httpMethod", equalTo(interceptedInteraction.httpMethod)))
            } else {
                requestPatternBuilder.withRequestBody(matchingJsonPath("$.httpMethod", absent()))
            }
            requestPatternBuilder.withRequestBody(matchingJsonPath("$.interactionType", equalTo(interceptedInteraction.interactionType.name)))
            requestPatternBuilder.withRequestBody(matchingJsonPath("$.profile", equalTo(interceptedInteraction.profile)))
            verify(requestPatternBuilder)
        }
    }
}
