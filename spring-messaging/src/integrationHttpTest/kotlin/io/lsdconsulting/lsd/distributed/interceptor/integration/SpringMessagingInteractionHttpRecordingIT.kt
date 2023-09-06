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
import io.lsdconsulting.lsd.distributed.interceptor.integration.testapp.config.InputRabbitMqConfig
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
import org.springframework.beans.factory.annotation.Value
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
private const val NO_ROUTING_KEY = ""

@SpringBootTest(webEnvironment = RANDOM_PORT, classes = [TestApplication::class])
@ActiveProfiles("test")
@Import(ServiceConfig::class, InputRabbitMqConfig::class)
@EmbeddedKafka(
    brokerProperties = ["log.dir=build/kafka_broker_logs",
        "listeners=PLAINTEXT://localhost:9093", "auto.create.topics.enable=true"]
)
class SpringMessagingInteractionHttpRecordingIT(
    @Value("\${spring.cloud.stream.bindings.inputOutputHandlerFunction-in-0.destination}")
    private val inputExchange: String,
    @Value("\${spring.cloud.stream.bindings.noOutputLsdHeadersHandlerFunction-in-0.destination}")
    private val noLsdHeadersInputExchange: String,
) {

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
            inputExchange, NO_ROUTING_KEY, MessageBuilder
                .withBody(ObjectMapperCreator().objectMapper.writeValueAsString(input).toByteArray())
                .setHeader(CONTENT_TYPE, APPLICATION_JSON)
                .setHeader("Source-Name", "SourceService")
                .setHeader("Target-Name", "InputEvent")
                .setHeader("b3", "dbfb676cf98bee5c-dbfb676cf98bee5c-0")
                .build()
        )

        Awaitility.await().untilAsserted {
            val messages = testListener.getOutputTopic()
            assertThat(messages, not(empty()))
            assertThat(messages, hasSize(1))
            assertThat(messages.map { it.payload }, hasItem(containsString("value")))
        }

        verify(
            buildExpectedInterceptedInteraction(
                traceId = "dbfb676cf98bee5c",
                body = "{\"id\":\"id\",\"value\":\"value\"}",
                target = "InputEvent",
                path = "Test App",
                interactionType = CONSUME
            )
        )

        verify(
            buildExpectedInterceptedInteraction(
                traceId = "dbfb676cf98bee5c",
                body = "{\"id\":\"id\",\"value\":\"value\",\"receivedDateTime\":",
                target = "output.topic",
                path = "output.topic",
                interactionType = PUBLISH
            )
        )
    }

    @Test
    fun `should handle no LSD headers in Rabbit input channel`() {
        val input = Input(id = "id", value = "value")

        rabbitTemplate.convertAndSend(
            inputExchange, NO_ROUTING_KEY, MessageBuilder
                .withBody(ObjectMapperCreator().objectMapper.writeValueAsString(input).toByteArray())
                .setHeader(CONTENT_TYPE, APPLICATION_JSON)
                .setHeader("b3", "dbfb676cf98bee5d-dbfb676cf98bee5d-0")
                .build()
        )

        Awaitility.await().untilAsserted {
            val messages = testListener.getOutputTopic()
            assertThat(messages, not(empty()))
            assertThat(messages, hasSize(1))
            assertThat(messages.map { it.payload }, hasItem(containsString("value")))
        }

        verify(
            buildExpectedInterceptedInteraction(
                traceId = "dbfb676cf98bee5d",
                body = "{\"id\":\"id\",\"value\":\"value\"}",
                target = "input.queue",
                path = "Test App",
                interactionType = CONSUME
            )
        )

        verify(
            buildExpectedInterceptedInteraction(
                traceId = "dbfb676cf98bee5d",
                body = "{\"id\":\"id\",\"value\":\"value\",\"receivedDateTime\":",
                target = "output.topic",
                path = "output.topic",
                interactionType = PUBLISH
            )
        )
    }

    @Test
    fun `should handle no LSD headers in Kafka output channel`() {
        val input = Input(id = "id", value = "value")

        rabbitTemplate.convertAndSend(
            noLsdHeadersInputExchange, NO_ROUTING_KEY, MessageBuilder
                .withBody(ObjectMapperCreator().objectMapper.writeValueAsString(input).toByteArray())
                .setHeader(CONTENT_TYPE, APPLICATION_JSON)
                .setHeader("b3", "dbfb676cf98bee5e-dbfb676cf98bee5e-0")
                .build()
        )

        Awaitility.await().untilAsserted {
            val messages = testListener.getNoLsdHeadersOutputTopic()
            assertThat(messages, not(empty()))
            assertThat(messages, hasSize(1))
            assertThat(messages.map { it.payload }, hasItem(containsString("value")))
        }

        verify(
            buildExpectedInterceptedInteraction(
                traceId = "dbfb676cf98bee5e",
                body = "{\"id\":\"id\",\"value\":\"value\"}",
                target = "no-lsd-headers.input.queue",
                path = "Test App",
                interactionType = CONSUME
            )
        )

        verify(
            buildExpectedInterceptedInteraction(
                traceId = "dbfb676cf98bee5e",
                body = "{\"id\":\"id\",\"value\":\"value\",\"receivedDateTime\":",
                target = "application.noOutputLsdHeadersHandlerFunction-out-0",
                path = "application.noOutputLsdHeadersHandlerFunction-out-0",
                interactionType = PUBLISH
            )
        )
    }

    private fun buildExpectedInterceptedInteraction(
        traceId: String,
        body: String?,
        target: String,
        path: String,
        interactionType: InteractionType
    ) = InterceptedInteraction(
        traceId = traceId,
        body = body,
        serviceName = "Test App",
        target = target,
        path = path,
        httpStatus = null,
        httpMethod = null,
        interactionType = interactionType,
        profile = NO_ROUTING_KEY,
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
