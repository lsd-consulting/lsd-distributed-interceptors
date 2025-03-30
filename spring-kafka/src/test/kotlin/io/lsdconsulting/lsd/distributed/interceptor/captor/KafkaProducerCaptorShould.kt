package io.lsdconsulting.lsd.distributed.interceptor.captor

import io.lsdconsulting.lsd.distributed.connector.model.InteractionType.PUBLISH
import io.lsdconsulting.lsd.distributed.interceptor.captor.common.PropertyServiceNameDeriver
import io.lsdconsulting.lsd.distributed.interceptor.captor.trace.TraceIdRetriever
import io.lsdconsulting.lsd.distributed.interceptor.persistence.RepositoryService
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.apache.commons.lang3.RandomStringUtils.secure
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.header.internals.RecordHeader
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.*
import org.junit.jupiter.api.Test

internal class KafkaProducerCaptorShould {
    private val repositoryService = mockk<RepositoryService>(relaxed = true)
    private val propertyServiceNameDeriver = mockk<PropertyServiceNameDeriver>()
    private val traceIdRetriever = mockk<TraceIdRetriever>()
    private val kafkaHeaderRetriever = mockk<KafkaHeaderRetriever>()

    private val underTest = KafkaProducerCaptor(repositoryService, propertyServiceNameDeriver, traceIdRetriever, kafkaHeaderRetriever, "profile")

    private val topic = secure().nextAlphabetic(20)
    private val serviceName = secure().nextAlphabetic(20)
    private val traceId = secure().nextAlphabetic(20)
    private val body = secure().nextAlphabetic(20)

    @Test
    fun `capture publish interaction with source from header`() {
        every { traceIdRetriever.getTraceId(any()) } returns traceId
        every { kafkaHeaderRetriever.retrieve(any()) } returns mapOf<String, Collection<String>>("name" to listOf("value"))
        val headers = listOf(RecordHeader("name", "value".toByteArray()), RecordHeader("Source-Name", serviceName.toByteArray()), RecordHeader("Target-Name", topic.toByteArray()))
        val message: ProducerRecord<String, Any> = ProducerRecord(topic, 0, "key", body.toByteArray(), headers)

        val result = underTest.capturePublishInteraction(message)

        assertThat(result.target, `is`(topic))
        assertThat(result.path, `is`(topic))
        assertThat(result.body, `is`(body))
        assertThat(result.serviceName, `is`(serviceName))
        assertThat(result.traceId, `is`(traceId))
        assertThat(result.interactionType, `is`(PUBLISH))
        assertThat(result.httpMethod, emptyOrNullString())
        assertThat(result.httpStatus, emptyOrNullString())
        assertThat(result.profile, `is`("profile"))
        assertThat(result.requestHeaders, `is`(mapOf("name" to listOf("value"))))
        assertThat(result.responseHeaders, aMapWithSize(0))
        verify { repositoryService.enqueue(result) }
    }

    @Test
    fun `capture publish interaction without source from header`() {
        every { propertyServiceNameDeriver.serviceName } returns serviceName
        every { traceIdRetriever.getTraceId(any()) } returns traceId
        every { kafkaHeaderRetriever.retrieve(any()) } returns mapOf<String, Collection<String>>("name" to listOf("value"))
        val headers = listOf(RecordHeader("name", "value".toByteArray()), RecordHeader("Target-Name", topic.toByteArray()))
        val message: ProducerRecord<String, Any> = ProducerRecord(topic, 0, "key", body.toByteArray(), headers)

        val result = underTest.capturePublishInteraction(message)

        assertThat(result.target, `is`(topic))
        assertThat(result.path, `is`(topic))
        assertThat(result.body, `is`(body))
        assertThat(result.serviceName, `is`(serviceName))
        assertThat(result.traceId, `is`(traceId))
        assertThat(result.interactionType, `is`(PUBLISH))
        assertThat(result.httpMethod, emptyOrNullString())
        assertThat(result.httpStatus, emptyOrNullString())
        assertThat(result.profile, `is`("profile"))
        assertThat(result.requestHeaders, `is`(mapOf("name" to listOf("value"))))
        assertThat(result.responseHeaders, aMapWithSize(0))
        verify { repositoryService.enqueue(result) }
    }

    @Test
    fun `capture publish interaction without target from header`() {
        every { propertyServiceNameDeriver.serviceName } returns serviceName
        every { traceIdRetriever.getTraceId(any()) } returns traceId
        every { kafkaHeaderRetriever.retrieve(any()) } returns mapOf<String, Collection<String>>("name" to listOf("value"))
        val headers = listOf(RecordHeader("name", "value".toByteArray()))
        val message: ProducerRecord<String, Any> = ProducerRecord(topic, 0, "key", body.toByteArray(), headers)

        val result = underTest.capturePublishInteraction(message)

        assertThat(result.target, `is`(topic))
        assertThat(result.path, `is`(topic))
        assertThat(result.body, `is`(body))
        assertThat(result.serviceName, `is`(serviceName))
        assertThat(result.traceId, `is`(traceId))
        assertThat(result.interactionType, `is`(PUBLISH))
        assertThat(result.httpMethod, emptyOrNullString())
        assertThat(result.httpStatus, emptyOrNullString())
        assertThat(result.profile, `is`("profile"))
        assertThat(result.requestHeaders, `is`(mapOf("name" to listOf("value"))))
        assertThat(result.responseHeaders, aMapWithSize(0))
        verify { repositoryService.enqueue(result) }
    }
}
