package io.lsdconsulting.lsd.distributed.interceptor.kafkaintegration

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.getAllServeEvents
import com.lsdconsulting.generatorui.controller.LsdControllerStub
import io.lsdconsulting.lsd.distributed.connector.model.InteractionType
import io.lsdconsulting.lsd.distributed.connector.model.InteractionType.CONSUME
import io.lsdconsulting.lsd.distributed.connector.model.InteractionType.PUBLISH
import io.lsdconsulting.lsd.distributed.connector.model.InterceptedInteraction
import io.lsdconsulting.lsd.distributed.interceptor.captor.common.print
import io.lsdconsulting.lsd.distributed.interceptor.config.mapper.ObjectMapperCreator
import io.lsdconsulting.lsd.distributed.interceptor.kafkaintegration.testapp.Input
import io.lsdconsulting.lsd.distributed.interceptor.kafkaintegration.testapp.Output
import io.lsdconsulting.lsd.distributed.interceptor.kafkaintegration.testapp.TestApplication
import lsd.logging.log
import org.apache.kafka.clients.consumer.CommitFailedException
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.header.internals.RecordHeader
import org.apache.kafka.common.serialization.StringDeserializer
import org.awaitility.Awaitility
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.*
import org.jeasy.random.EasyRandom
import org.jeasy.random.EasyRandomParameters
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.kafka.support.serializer.JsonDeserializer
import org.springframework.kafka.test.context.EmbeddedKafka
import org.springframework.test.context.ActiveProfiles
import java.time.Duration
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit.MILLIS
import java.util.*


private const val WIREMOCK_SERVER_PORT = 8070
private const val NO_ROUTING_KEY = ""

@SpringBootTest(webEnvironment = RANDOM_PORT, classes = [TestApplication::class])
@ActiveProfiles("test")
@EmbeddedKafka(
    brokerProperties = ["log.dir=build/kafka_broker_logs",
        "listeners=PLAINTEXT://localhost:9095", "auto.create.topics.enable=true"]
)
class KafkaInteractionHttpRecordingIT(
    @Value("\${service.incomingTopic}")
    private val incomingTopic: String,
) {

    private val mapper = ObjectMapperCreator().objectMapper
    private val lsdControllerStub = LsdControllerStub(mapper)
    private val easyRandom: EasyRandom = EasyRandom(EasyRandomParameters().seed(System.currentTimeMillis()))

    @BeforeEach
    fun setup() {
        WireMock.reset()
        lsdControllerStub.store(easyRandom.nextObject(InterceptedInteraction::class.java))
    }

    @Test
    fun `should record interactions from intercepted Kafka messages`() {
        val input = Input(id = "id", value = "value")
        val consumer = setupKafkaConsumer()
        consumer.subscribe(listOf("outgoingTopic"))

        KafkaProducer<String, Input>(getProducerProperties()).send(
            ProducerRecord(
                incomingTopic, null, null, null, input, listOf(
                    RecordHeader("Source-Name", "Service1".toByteArray()),
                    RecordHeader("Target-Name", "SomeEvent".toByteArray()),
                    RecordHeader("b3", "dbfb676cf98bee5d-dbfb676cf98bee5d-0".toByteArray())
                )
            )
        )

        val consumerRecords = mutableListOf<ConsumerRecord<String, Output>>()
        Awaitility.await().untilAsserted {
            consumerRecords.addAll(consumer.poll(Duration.of(1000, MILLIS)).toList())
            try {
                consumer.commitSync()
            } catch (e: CommitFailedException) {
                log().error("Commit failed", e)
                throw e
            }
            assertThat(consumerRecords, not(empty()))
            assertThat(consumerRecords, hasSize(1))
            assertThat(
                consumerRecords.map { it.value().toString() },
                hasItem(containsString("value"))
            )
            assertThat(getAllServeEvents(), hasSize(4))
        }

        val output = consumerRecords.first().value()

        verify(
            buildExpectedInterceptedInteraction(
                traceId = "dbfb676cf98bee5d",
                serviceName = "Service1",
                body = print(input),
                target = "SomeEvent",
                path = "SomeEvent",
                interactionType = PUBLISH
            )
        )

        verify(
            buildExpectedInterceptedInteraction(
                traceId = "dbfb676cf98bee5d",
                serviceName = "Service2",
                body = print(input),
                target = "SomeEvent",
                path = "SomeEvent",
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

    @Test
    fun `should handle no LSD headers in Kafka messages`() {
        val input = Input(id = "id", value = "value")
        val consumer = setupKafkaConsumer()
        consumer.subscribe(listOf("outgoingTopic"))

        KafkaProducer<String, Input>(getProducerProperties()).send(
            ProducerRecord(
                incomingTopic, null, null, null, input, listOf(
                    RecordHeader("b3", "dbfb676cf98bee5c-dbfb676cf98bee5c-0".toByteArray())
                )
            )
        )

        val consumerRecords = mutableListOf<ConsumerRecord<String, Output>>()
        Awaitility.await().untilAsserted {
            consumerRecords.addAll(consumer.poll(Duration.of(1000, MILLIS)).toList())
            try {
                consumer.commitSync()
            } catch (e: CommitFailedException) {
                log().error("Commit failed", e)
                throw e
            }
            assertThat(consumerRecords, not(empty()))
            assertThat(consumerRecords, hasSize(1))
            assertThat(
                consumerRecords.map { it.value().toString() },
                hasItem(containsString("value"))
            )
            assertThat(getAllServeEvents(), hasSize(4))
        }

        val output = consumerRecords.first().value()


        verify(
            buildExpectedInterceptedInteraction(
                traceId = "dbfb676cf98bee5c",
                serviceName = "Service2", // Should be `Service1` but there is only one `info.app.name` within the test
                body = print(input),
                target = "incomingTopic",
                path = "incomingTopic",
                interactionType = PUBLISH
            )
        )

        verify(
            buildExpectedInterceptedInteraction(
                traceId = "dbfb676cf98bee5c",
                serviceName = "Service2",
                body = print(input),
                target = "incomingTopic",
                path = "incomingTopic",
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
        val requestPatternBuilder = WireMock.postRequestedFor(WireMock.urlPathEqualTo("/lsds"))
        requestPatternBuilder.withRequestBody(
            WireMock.matchingJsonPath(
                "$.traceId",
                WireMock.equalTo(interceptedInteraction.traceId)
            )
        )
        if (interceptedInteraction.body != null) {
            requestPatternBuilder.withRequestBody(
                WireMock.matchingJsonPath(
                    "$.body",
                    WireMock.containing(interceptedInteraction.body)
                )
            )
        } else {
            requestPatternBuilder.withRequestBody(WireMock.matchingJsonPath("$.body", WireMock.absent()))
        }
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

    companion object {
        private val wireMockServer = WireMockServer(WIREMOCK_SERVER_PORT)

        @BeforeAll
        @JvmStatic
        internal fun beforeAll() {
            System.setProperty("lsd.dist.connectionString", "http://localhost:8070")
            System.setProperty("info.app.name", "Service2")
            WireMock.configureFor(WIREMOCK_SERVER_PORT)
            wireMockServer.start()
        }

        @AfterAll
        @JvmStatic
        internal fun afterAll() {
            wireMockServer.stop()
        }
    }

    private fun getProducerProperties(): Properties {
        val producerProperties = Properties()
        producerProperties["interceptor.classes"] =
            "io.lsdconsulting.lsd.distributed.interceptor.interceptor.LsdKafkaInterceptor"
        producerProperties["key.serializer"] = "org.apache.kafka.common.serialization.StringSerializer"
        producerProperties["value.serializer"] = "org.springframework.kafka.support.serializer.JsonSerializer"
        producerProperties["bootstrap.servers"] = "localhost:9095"
        return producerProperties
    }

    private fun setupKafkaConsumer(): KafkaConsumer<String, Output> {
        val consumerProperties = Properties()
        consumerProperties.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9095")
        consumerProperties.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer::class.java.getName())
        consumerProperties.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer::class.java.getName())
        consumerProperties.setProperty(ConsumerConfig.GROUP_ID_CONFIG, "someGroup")
        consumerProperties.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")
        consumerProperties.setProperty(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG, "1000")
        consumerProperties.setProperty("spring.json.trusted.packages", "*")
        consumerProperties.setProperty("info.app.name", "Service1")
        consumerProperties["interceptor.classes"] =
            "io.lsdconsulting.lsd.distributed.interceptor.interceptor.LsdKafkaInterceptor"
        val consumer = KafkaConsumer<String, Output>(consumerProperties)
        return consumer
    }
}
