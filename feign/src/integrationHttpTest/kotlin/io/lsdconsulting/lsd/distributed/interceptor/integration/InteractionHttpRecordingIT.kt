package io.lsdconsulting.lsd.distributed.interceptor.integration

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.*
import com.lsdconsulting.generatorui.controller.LsdControllerStub
import io.lsdconsulting.lsd.distributed.connector.model.InteractionType
import io.lsdconsulting.lsd.distributed.connector.model.InteractionType.*
import io.lsdconsulting.lsd.distributed.connector.model.InterceptedInteraction
import io.lsdconsulting.lsd.distributed.interceptor.integration.testapp.TestApplication
import io.lsdconsulting.lsd.distributed.interceptor.integration.testapp.external.ExternalClient
import io.lsdconsulting.lsd.distributed.interceptor.integration.testapp.external.ExternalClientWithTargetHeader
import org.awaitility.Awaitility
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.jeasy.random.EasyRandom
import org.jeasy.random.EasyRandomParameters
import org.junit.jupiter.api.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.*


private const val WIREMOCK_SERVER_PORT = 8070

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT, classes = [TestApplication::class])
@ActiveProfiles("test")
class InteractionHttpRecordingIT {
    private val mapper = ObjectMapper()
    private val lsdControllerStub = LsdControllerStub(mapper)

    private val easyRandom: EasyRandom = EasyRandom(EasyRandomParameters().seed(System.currentTimeMillis()))

    @Autowired
    private lateinit var externalClient: ExternalClient

    @Autowired
    private lateinit var externalClientWithTargetHeader: ExternalClientWithTargetHeader

    @BeforeEach
    fun setup() {
        reset()
        mapper.registerModule(KotlinModule.Builder().build())
        mapper.registerModule(JavaTimeModule())
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        lsdControllerStub.store(easyRandom.nextObject(InterceptedInteraction::class.java))
    }

    @Test
    fun `should record feign client interactions`() {
        val response = externalClient.get("from_test_client")
        assertThat(response, `is`("Response:from_test_client"))

        // Assert calls to http storage endpoint
        Awaitility.await().untilAsserted {
            verify(2, postRequestedFor(urlPathEqualTo("/lsds")))
            verify(
                buildExpectedInterceptedInteraction(
                    body = null,
                    serviceName = "TestApp",
                    target = "UNKNOWN_TARGET",
                    path = "/get-api?message=from_test_client",
                    httpStatus = null,
                    httpMethod = "GET",
                    interactionType = REQUEST
                )
            )
            verify(
                buildExpectedInterceptedInteraction(
                    body = "Response:from_test_client",
                    serviceName = "TestApp",
                    target = "UNKNOWN_TARGET",
                    path = "/get-api?message=from_test_client",
                    httpStatus = "200 OK",
                    httpMethod = null,
                    interactionType = RESPONSE
                )
            )
        }
    }

    @Test
    fun `should record interactions with supplied names through headers`() {
        val response = externalClientWithTargetHeader.post("from_test_client_with_target_header")
        assertThat(response, `is`("Response:from_test_client_with_target_header"))

        // Assert calls to http storage endpoint
        Awaitility.await().untilAsserted {
            verify(2, postRequestedFor(urlPathEqualTo("/lsds")))
            verify(
                buildExpectedInterceptedInteraction(
                    "from_test_client_with_target_header",
                    serviceName = "Upstream",
                    target = "Downstream",
                    path = "/post-api",
                    httpStatus = null,
                    httpMethod = "POST",
                    interactionType = REQUEST
                )
            )
            verify(
                buildExpectedInterceptedInteraction(
                    "Response:from_test_client_with_target_header",
                    serviceName = "Upstream",
                    target = "Downstream",
                    path = "/post-api",
                    httpStatus = "200 OK",
                    httpMethod = null,
                    interactionType = RESPONSE
                )
            )
        }
    }

    private fun buildExpectedInterceptedInteraction(
        body: String?,
        serviceName: String,
        target: String,
        path: String,
        httpStatus: String?,
        httpMethod: String?,
        interactionType: InteractionType
    ) = InterceptedInteraction(
        traceId = "3e316fc2da26a3c7",
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

    private fun verify(interceptedInteraction: InterceptedInteraction) {
        val requestPatternBuilder = postRequestedFor(urlPathEqualTo("/lsds"))
        requestPatternBuilder.withRequestBody(matchingJsonPath("$.traceId", equalTo(interceptedInteraction.traceId)))
        if (interceptedInteraction.body != null) {
            requestPatternBuilder.withRequestBody(matchingJsonPath("$.body", equalTo(interceptedInteraction.body)))
        } else {
            requestPatternBuilder.withRequestBody(matchingJsonPath("$.body", absent()))
        }
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

    companion object {
        private val wireMockServer = WireMockServer(WIREMOCK_SERVER_PORT)

        @BeforeAll
        @JvmStatic
        internal fun beforeAll() {
            configureFor(WIREMOCK_SERVER_PORT)
            wireMockServer.start()
        }

        @AfterAll
        @JvmStatic
        internal fun afterAll() {
            wireMockServer.stop()
        }
    }
}
