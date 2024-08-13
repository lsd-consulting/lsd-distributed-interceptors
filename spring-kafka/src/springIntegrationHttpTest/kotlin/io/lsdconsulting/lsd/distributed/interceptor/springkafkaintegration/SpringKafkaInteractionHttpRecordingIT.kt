package io.lsdconsulting.lsd.distributed.interceptor.springkafkaintegration

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.*
import com.lsdconsulting.generatorui.controller.LsdControllerStub
import io.lsdconsulting.lsd.distributed.connector.model.InteractionType
import io.lsdconsulting.lsd.distributed.connector.model.InteractionType.CONSUME
import io.lsdconsulting.lsd.distributed.connector.model.InteractionType.PUBLISH
import io.lsdconsulting.lsd.distributed.connector.model.InterceptedInteraction
import io.lsdconsulting.lsd.distributed.interceptor.captor.common.print
import io.lsdconsulting.lsd.distributed.interceptor.config.mapper.ObjectMapperCreator
import io.lsdconsulting.lsd.distributed.interceptor.springkafkaintegration.testapp.Input
import io.lsdconsulting.lsd.distributed.interceptor.springkafkaintegration.testapp.TestApplication
import io.lsdconsulting.lsd.distributed.interceptor.springkafkaintegration.testapp.TestListener
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.header.internals.RecordHeader
import org.awaitility.Awaitility
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.*
import org.jeasy.random.EasyRandom
import org.jeasy.random.EasyRandomParameters
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.test.context.EmbeddedKafka
import org.springframework.test.context.ActiveProfiles
import java.time.ZoneId
import java.time.ZonedDateTime

private const val WIREMOCK_SERVER_PORT = 8070
private const val NO_ROUTING_KEY = ""

@SpringBootTest(webEnvironment = RANDOM_PORT, classes = [TestApplication::class])
@ActiveProfiles("test")
@EmbeddedKafka(
    brokerProperties = ["log.dir=build/spring_kafka_broker_logs",
        "listeners=PLAINTEXT://localhost:9094", "auto.create.topics.enable=true"]
)
class SpringKafkaInteractionHttpRecordingIT(
    @Value("\${service.incomingTopic}")
    private val incomingTopic: String,
) {

    @Autowired
    private lateinit var kafkaTemplate: KafkaTemplate<String, Input>

    @Autowired
    private lateinit var testListener: TestListener

    private val mapper = ObjectMapperCreator().objectMapper
    private val lsdControllerStub = LsdControllerStub(mapper)
    private val easyRandom: EasyRandom = EasyRandom(EasyRandomParameters().seed(System.currentTimeMillis()))

    @BeforeEach
    fun setup() {
        reset()
        lsdControllerStub.store(easyRandom.nextObject(InterceptedInteraction::class.java))
        testListener.clearOutgoingTopic()
    }

    @Test
    fun `should record interactions from intercepted Kafka messages`() {
        val input = Input(id = "id", value = "value")
        kafkaTemplate.send(
            ProducerRecord(
                incomingTopic, null, null, null, input, listOf(
                    RecordHeader("Source-Name", "Service1".toByteArray()),
                    RecordHeader("Target-Name", "SomeEvent".toByteArray()),
                    RecordHeader("b3", "dbfb676cf98bee5c-dbfb676cf98bee5c-0".toByteArray())
                )
            )
        )

        Awaitility.await().untilAsserted {
            val messages = testListener.getOutgoingTopic()
            assertThat(messages, not(empty()))
            assertThat(messages, hasSize(1))
            assertThat(messages.map { it.payload.toString() }, hasItem(containsString("value")))
            assertThat(getAllServeEvents(), hasSize(4))
        }

        val output = testListener.getOutgoingTopic().first().payload

        verify(
            buildExpectedInterceptedInteraction(
                traceId = "dbfb676cf98bee5c",
                serviceName = "Service1",
                body = print(input),
                target = "SomeEvent",
                path = "SomeEvent",
                interactionType = PUBLISH
            )
        )

        verify(
            buildExpectedInterceptedInteraction(
                traceId = "dbfb676cf98bee5c",
                serviceName = "Service2",
                body = print(input),
                target = "SomeEvent",
                path = "SomeEvent",
                interactionType = CONSUME
            )
        )

        verify(
            buildExpectedInterceptedInteraction(
                traceId = "dbfb676cf98bee5c",
                serviceName = "Service2",
                body = print(output),
                target = "NewEvent",
                path = "NewEvent",
                interactionType = PUBLISH
            )
        )

        verify(
            buildExpectedInterceptedInteraction(
                traceId = "dbfb676cf98bee5c",
                serviceName = "Service2", // Should be `Service1` but there is only one `info.app.name` within the test
                body = print(output),
                target = "NewEvent",
                path = "NewEvent",
                interactionType = CONSUME
            )
        )
    }

    @Test
    fun `should handle no LSD headers in Kafka messages`() {
        val input = Input(id = "id", value = "value")

        kafkaTemplate.send(
            ProducerRecord(
                incomingTopic, null, null, null, input, listOf(
                    RecordHeader("b3", "dbfb676cf98bee5d-dbfb676cf98bee5d-0".toByteArray())
                )
            )
        )

        Awaitility.await().untilAsserted {
            val messages = testListener.getOutgoingTopic()
            assertThat(messages, not(empty()))
            assertThat(messages, hasSize(1))
            assertThat(messages.map { it.payload.toString() }, hasItem(containsString("value")))
            assertThat(getAllServeEvents(), hasSize(4))
        }

        val output = testListener.getOutgoingTopic().first().payload

        verify(
            buildExpectedInterceptedInteraction(
                traceId = "dbfb676cf98bee5d",
                serviceName = "Service2", // Should be `Service1` but there is only one `info.app.name` within the test
                body = print(input),
                target = "incomingTopic",
                path = "incomingTopic",
                interactionType = PUBLISH
            )
        )

        verify(
            buildExpectedInterceptedInteraction(
                traceId = "dbfb676cf98bee5d",
                serviceName = "Service2",
                body = print(input),
                target = "incomingTopic",
                path = "incomingTopic",
                interactionType = CONSUME
            )
        )

        verify(
            buildExpectedInterceptedInteraction(
                traceId = "dbfb676cf98bee5d",
                serviceName = "Service2",
                body = print(output),
                target = "NewEvent",
                path = "NewEvent",
                interactionType = PUBLISH
            )
        )

        verify(
            buildExpectedInterceptedInteraction(
                traceId = "dbfb676cf98bee5d",
                serviceName = "Service2", // Should be `Service1` but there is only one `info.app.name` within the test
                body = print(output),
                target = "NewEvent",
                path = "NewEvent",
                interactionType = CONSUME
            )
        )
    }

    private fun buildExpectedInterceptedInteraction(
        traceId: String,
        serviceName: String,
        body: String?,
        target: String,
        path: String,
        interactionType: InteractionType
    ) = InterceptedInteraction(
        traceId = traceId,
        body = body,
        serviceName = serviceName,
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
