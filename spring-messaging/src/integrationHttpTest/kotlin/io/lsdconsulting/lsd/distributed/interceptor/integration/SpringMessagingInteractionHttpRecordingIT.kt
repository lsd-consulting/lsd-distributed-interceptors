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
import io.lsdconsulting.lsd.distributed.interceptor.config.mapper.ObjectMapperCreator
import io.lsdconsulting.lsd.distributed.interceptor.integration.testapp.TestApplication
import io.lsdconsulting.lsd.distributed.interceptor.integration.testapp.config.RabbitMqConfig
import io.lsdconsulting.lsd.distributed.interceptor.integration.testapp.config.ServiceConfig
import io.lsdconsulting.lsd.distributed.interceptor.integration.testapp.handler.Input
import io.lsdconsulting.lsd.distributed.interceptor.integration.testapp.handler.TestListener
import org.awaitility.Awaitility
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.*
import org.jeasy.random.EasyRandom
import org.jeasy.random.EasyRandomParameters
import org.junit.jupiter.api.*
import org.springframework.amqp.core.MessageBuilder
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.context.annotation.Import
import org.springframework.kafka.test.context.EmbeddedKafka
import org.springframework.messaging.MessageHeaders.CONTENT_TYPE
import org.springframework.test.context.ActiveProfiles
import org.springframework.util.MimeTypeUtils.APPLICATION_JSON
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.*

private const val WIREMOCK_SERVER_PORT = 8070

@SpringBootTest(webEnvironment = RANDOM_PORT, classes = [TestApplication::class])
@ActiveProfiles("test")
@Import(ServiceConfig::class, RabbitMqConfig::class)
@EmbeddedKafka(
    brokerProperties = ["log.dir=build/kafka_broker_logs",
        "listeners=PLAINTEXT://localhost:9093", "auto.create.topics.enable=true"]
)
class SpringMessagingInteractionHttpRecordingIT {

    @Autowired
    private lateinit var rabbitTemplate: RabbitTemplate

    @Autowired
    private lateinit var testListener: TestListener

    private val mapper = ObjectMapper()
    private val lsdControllerStub = LsdControllerStub(mapper)

    private val easyRandom: EasyRandom = EasyRandom(EasyRandomParameters().seed(System.currentTimeMillis()))

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
    fun `should record interactions from intercepted channel headers`() {
        val input = Input(id = "id", value = "value")


        rabbitTemplate.convertAndSend(
            "input.fanout", "", MessageBuilder
                .withBody(ObjectMapperCreator().objectMapper.writeValueAsString(input).toByteArray())
                .setHeader(CONTENT_TYPE, APPLICATION_JSON)
                .setHeader("Source-Name", "SourceService")
                .setHeader("Target-Name", "InputEvent")
                .setHeader("b3", "dbfb676cf98bee5c-dbfb676cf98bee5c-0")
                .build()
        )

        Awaitility.await().untilAsserted {
            val messages = testListener.getOutputQueue()
            assertThat(messages, not(empty()))
            assertThat(messages, hasSize(1))
            assertThat(messages.map { it.payload }, hasItem(containsString("value")))
        }


        verify(
            buildExpectedInterceptedInteraction(
                body = "{\"id\":\"id\",\"value\":\"value\"}",
                serviceName = "TestApp",
                target = "InputEvent",
                path = "TestApp",
                httpStatus = null,
                httpMethod = null,
                interactionType = CONSUME
            )
        )

        verify(
            buildExpectedInterceptedInteraction(
                body = "{\"id\":\"id\",\"value\":\"value\",\"receivedDateTime\":",
                serviceName = "TestApp",
                target = "output.topic",
                path = "output.topic",
                httpStatus = null,
                httpMethod = null,
                interactionType = PUBLISH
            )
        )
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
        traceId = "dbfb676cf98bee5c",
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
            requestPatternBuilder.withRequestBody(matchingJsonPath("$.body", containing(interceptedInteraction.body)))
        } else {
            requestPatternBuilder.withRequestBody(matchingJsonPath("$.body", absent()))
        }
        requestPatternBuilder.withRequestBody(
            matchingJsonPath(
                "$.serviceName",
                equalTo(interceptedInteraction.serviceName)
            )
        )
        requestPatternBuilder.withRequestBody(matchingJsonPath("$.target", equalTo(interceptedInteraction.target)))
        requestPatternBuilder.withRequestBody(matchingJsonPath("$.path", equalTo(interceptedInteraction.path)))
        if (interceptedInteraction.httpStatus != null) {
            requestPatternBuilder.withRequestBody(
                matchingJsonPath(
                    "$.httpStatus",
                    equalTo(interceptedInteraction.httpStatus)
                )
            )
        } else {
            requestPatternBuilder.withRequestBody(matchingJsonPath("$.httpStatus", absent()))
        }
        if (interceptedInteraction.httpMethod != null) {
            requestPatternBuilder.withRequestBody(
                matchingJsonPath(
                    "$.httpMethod",
                    equalTo(interceptedInteraction.httpMethod)
                )
            )
        } else {
            requestPatternBuilder.withRequestBody(matchingJsonPath("$.httpMethod", absent()))
        }
        requestPatternBuilder.withRequestBody(
            matchingJsonPath(
                "$.interactionType",
                equalTo(interceptedInteraction.interactionType.name)
            )
        )
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
