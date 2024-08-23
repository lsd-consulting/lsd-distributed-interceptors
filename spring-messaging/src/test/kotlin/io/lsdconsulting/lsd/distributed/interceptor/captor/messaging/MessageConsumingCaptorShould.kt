package io.lsdconsulting.lsd.distributed.interceptor.captor.messaging

import io.lsdconsulting.lsd.distributed.connector.model.InteractionType.CONSUME
import io.lsdconsulting.lsd.distributed.interceptor.captor.common.PropertyServiceNameDeriver
import io.lsdconsulting.lsd.distributed.interceptor.captor.trace.TraceIdRetriever
import io.lsdconsulting.lsd.distributed.interceptor.persistence.RepositoryService
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.apache.commons.lang3.RandomStringUtils.randomAlphabetic
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.*
import org.junit.jupiter.api.Test
import org.springframework.integration.support.MutableMessage
import org.springframework.messaging.Message

internal class MessageConsumingCaptorShould {
    private val repositoryService = mockk<RepositoryService>(relaxed = true)
    private val propertyServiceNameDeriver = mockk<PropertyServiceNameDeriver>()
    private val traceIdRetriever = mockk<TraceIdRetriever>()
    private val messagingHeaderRetriever = mockk<MessagingHeaderRetriever>()

    private val underTest = MessageConsumingCaptor(repositoryService, propertyServiceNameDeriver, traceIdRetriever, messagingHeaderRetriever, "profile")

    private val targetHeader = randomAlphabetic(20)
    private val typeId = randomAlphabetic(20)
    private val serviceName = randomAlphabetic(20)
    private val traceId = randomAlphabetic(20)
    private val body = randomAlphabetic(20)

    @Test
    fun `capture consume interaction with source from type id when target name not no in header`() {
        every { propertyServiceNameDeriver.serviceName } returns serviceName
        every { traceIdRetriever.getTraceId(any()) } returns traceId
        every { messagingHeaderRetriever.retrieve(any()) } returns mapOf<String, Collection<String>>("name" to listOf("value"), "__TypeId__" to listOf(typeId))
        val headers = mapOf("name" to listOf("value"), "__TypeId__" to typeId)
        val message: Message<*> = MutableMessage(body.toByteArray(), headers)

        val result = underTest.captureConsumeInteraction(message)

        assertThat(result.target, `is`(typeId))
        assertThat(result.path, `is`(serviceName))
        assertThat(result.body, `is`(body))
        assertThat(result.serviceName, `is`(serviceName))
        assertThat(result.traceId, `is`(traceId))
        assertThat(result.interactionType, `is`(CONSUME))
        assertThat(result.httpMethod, emptyOrNullString())
        assertThat(result.httpStatus, emptyOrNullString())
        assertThat(result.profile, `is`("profile"))
        assertThat(result.requestHeaders, `is`(mapOf("name" to listOf("value"), "__TypeId__" to listOf(typeId))))
        assertThat(result.responseHeaders, aMapWithSize(0))
        verify { repositoryService.enqueue(result) }
    }

    @Test
    fun `capture consume interaction with default source when target not available`() {
        every { propertyServiceNameDeriver.serviceName } returns serviceName
        every { traceIdRetriever.getTraceId(any()) } returns traceId
        every { messagingHeaderRetriever.retrieve(any()) } returns mapOf<String, Collection<String>>("name" to listOf("value"))
        val headers = mapOf<String, Any>("name" to listOf("value"))
        val message: Message<*> = MutableMessage(body.toByteArray(), headers)

        val result = underTest.captureConsumeInteraction(message)

        assertThat(result.target, `is`("UNKNOWN"))
        assertThat(result.path, `is`(serviceName))
        assertThat(result.body, `is`(body))
        assertThat(result.serviceName, `is`(serviceName))
        assertThat(result.traceId, `is`(traceId))
        assertThat(result.interactionType, `is`(CONSUME))
        assertThat(result.httpMethod, emptyOrNullString())
        assertThat(result.httpStatus, emptyOrNullString())
        assertThat(result.profile, `is`("profile"))
        assertThat(result.requestHeaders, `is`(mapOf("name" to listOf("value"))))
        assertThat(result.responseHeaders, aMapWithSize(0))
        verify { repositoryService.enqueue(result) }
    }

    @Test
    fun `capture consume interaction with source from target name`() {
        every { propertyServiceNameDeriver.serviceName } returns serviceName
        every { traceIdRetriever.getTraceId(any()) } returns traceId
        every { messagingHeaderRetriever.retrieve(any()) } returns mapOf<String, Collection<String>>("name" to listOf("value"), "Target-Name" to listOf(targetHeader), "__TypeId__" to listOf(typeId))
        val headers = mapOf("name" to listOf("value"), "__TypeId__" to typeId, "Target-Name" to targetHeader)
        val message: Message<*> = MutableMessage(body.toByteArray(), headers)

        val result = underTest.captureConsumeInteraction(message)

        assertThat(result.target, `is`(targetHeader))
        assertThat(result.path, `is`(serviceName))
        assertThat(result.body, `is`(body))
        assertThat(result.serviceName, `is`(serviceName))
        assertThat(result.traceId, `is`(traceId))
        assertThat(result.interactionType, `is`(CONSUME))
        assertThat(result.httpMethod, emptyOrNullString())
        assertThat(result.httpStatus, emptyOrNullString())
        assertThat(result.profile, `is`("profile"))
        assertThat(result.requestHeaders, `is`(mapOf("name" to listOf("value"), "__TypeId__" to listOf(typeId), "Target-Name" to listOf(targetHeader))))
        assertThat(result.responseHeaders, aMapWithSize(0))
        verify { repositoryService.enqueue(result) }
    }
}
