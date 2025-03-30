package io.lsdconsulting.lsd.distributed.interceptor.captor

import io.lsdconsulting.lsd.distributed.connector.model.InteractionType.CONSUME
import io.lsdconsulting.lsd.distributed.interceptor.captor.common.PropertyServiceNameDeriver
import io.lsdconsulting.lsd.distributed.interceptor.captor.trace.TraceIdRetriever
import io.lsdconsulting.lsd.distributed.interceptor.persistence.RepositoryService
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.apache.commons.lang3.RandomStringUtils.secure
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.clients.consumer.ConsumerRecords
import org.apache.kafka.common.TopicPartition
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.*
import org.junit.jupiter.api.Test

internal class KafkaConsumerCaptorShould {
    private val repositoryService = mockk<RepositoryService>(relaxed = true)
    private val propertyServiceNameDeriver = mockk<PropertyServiceNameDeriver>()
    private val traceIdRetriever = mockk<TraceIdRetriever>()
    private val kafkaHeaderRetriever = mockk<KafkaHeaderRetriever>()

    private val underTest = KafkaConsumerCaptor(repositoryService, propertyServiceNameDeriver, traceIdRetriever, kafkaHeaderRetriever, "profile")

    private val targetHeader = secure().nextAlphabetic(20)
    private val topic = secure().nextAlphabetic(20)
    private val packageName = secure().nextAlphabetic(20)
    private val className = secure().nextAlphabetic(20)
    private val typeId = "$packageName.$className"
    private val serviceName = secure().nextAlphabetic(20)
    private val traceId = secure().nextAlphabetic(20)
    private val body = secure().nextAlphabetic(20)

    @Test
    fun `capture consume interaction with source from type id when target name not no in header`() {
        every { propertyServiceNameDeriver.serviceName } returns serviceName
        every { traceIdRetriever.getTraceId(any()) } returns traceId
        every { kafkaHeaderRetriever.retrieve(any()) } returns mapOf<String, Collection<String>>("name" to listOf("value"), "__TypeId__" to listOf(typeId))
        val consumerRecord: ConsumerRecord<String, Any> = ConsumerRecord(topic, 0, 0L, "key", body)
        val consumerRecords = ConsumerRecords(mapOf(TopicPartition(topic, 0) to listOf(consumerRecord)))

        val result = underTest.captureConsumeInteraction(consumerRecords)

        assertThat(result, hasSize(1))
        val interceptedInteraction = result.first()
        assertThat(interceptedInteraction.target, `is`(className))
        assertThat(interceptedInteraction.path, `is`(serviceName))
        assertThat(interceptedInteraction.body, `is`(body))
        assertThat(interceptedInteraction.serviceName, `is`(serviceName))
        assertThat(interceptedInteraction.traceId, `is`(traceId))
        assertThat(interceptedInteraction.interactionType, `is`(CONSUME))
        assertThat(interceptedInteraction.httpMethod, emptyOrNullString())
        assertThat(interceptedInteraction.httpStatus, emptyOrNullString())
        assertThat(interceptedInteraction.profile, `is`("profile"))
        assertThat(interceptedInteraction.requestHeaders, `is`(mapOf("name" to listOf("value"), "__TypeId__" to listOf(typeId))))
        assertThat(interceptedInteraction.responseHeaders, aMapWithSize(0))
        verify { repositoryService.enqueue(interceptedInteraction) }
    }

    @Test
    fun `capture consume interaction with default source when target not available`() {
        every { propertyServiceNameDeriver.serviceName } returns serviceName
        every { traceIdRetriever.getTraceId(any()) } returns traceId
        every { kafkaHeaderRetriever.retrieve(any()) } returns mapOf<String, Collection<String>>("name" to listOf("value"))
        val consumerRecord: ConsumerRecord<String, Any> = ConsumerRecord(topic, 0, 0L, "key", body)
        val consumerRecords = ConsumerRecords(mapOf(TopicPartition(topic, 0) to listOf(consumerRecord)))

        val result = underTest.captureConsumeInteraction(consumerRecords)

        assertThat(result, hasSize(1))
        val interceptedInteraction = result.first()
        assertThat(interceptedInteraction.target, `is`(topic))
        assertThat(interceptedInteraction.path, `is`(serviceName))
        assertThat(interceptedInteraction.body, `is`(body))
        assertThat(interceptedInteraction.serviceName, `is`(serviceName))
        assertThat(interceptedInteraction.traceId, `is`(traceId))
        assertThat(interceptedInteraction.interactionType, `is`(CONSUME))
        assertThat(interceptedInteraction.httpMethod, emptyOrNullString())
        assertThat(interceptedInteraction.httpStatus, emptyOrNullString())
        assertThat(interceptedInteraction.profile, `is`("profile"))
        assertThat(interceptedInteraction.requestHeaders, `is`(mapOf("name" to listOf("value"))))
        assertThat(interceptedInteraction.responseHeaders, aMapWithSize(0))
        verify { repositoryService.enqueue(interceptedInteraction) }
    }

    @Test
    fun `capture consume interaction with source from target name`() {
        every { propertyServiceNameDeriver.serviceName } returns serviceName
        every { traceIdRetriever.getTraceId(any()) } returns traceId
        every { kafkaHeaderRetriever.retrieve(any()) } returns mapOf<String, Collection<String>>("name" to listOf("value"), "Target-Name" to listOf(targetHeader), "__TypeId__" to listOf(typeId))
        val consumerRecord: ConsumerRecord<String, Any> = ConsumerRecord(topic, 0, 0L, "key", body)
        val consumerRecords = ConsumerRecords(mapOf(TopicPartition(topic, 0) to listOf(consumerRecord)))

        val result = underTest.captureConsumeInteraction(consumerRecords)

        assertThat(result, hasSize(1))
        val interceptedInteraction = result.first()
        assertThat(interceptedInteraction.target, `is`(targetHeader))
        assertThat(interceptedInteraction.path, `is`(serviceName))
        assertThat(interceptedInteraction.body, `is`(body))
        assertThat(interceptedInteraction.serviceName, `is`(serviceName))
        assertThat(interceptedInteraction.traceId, `is`(traceId))
        assertThat(interceptedInteraction.interactionType, `is`(CONSUME))
        assertThat(interceptedInteraction.httpMethod, emptyOrNullString())
        assertThat(interceptedInteraction.httpStatus, emptyOrNullString())
        assertThat(interceptedInteraction.profile, `is`("profile"))
        assertThat(interceptedInteraction.requestHeaders, `is`(mapOf("name" to listOf("value"), "__TypeId__" to listOf(typeId), "Target-Name" to listOf(targetHeader))))
        assertThat(interceptedInteraction.responseHeaders, aMapWithSize(0))
        verify { repositoryService.enqueue(interceptedInteraction) }
    }
}
