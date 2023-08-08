package io.lsdconsulting.lsd.distributed.interceptor.captor.messaging

import io.lsdconsulting.lsd.distributed.connector.model.InteractionType.PUBLISH
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
import org.springframework.integration.channel.PublishSubscribeChannel
import org.springframework.integration.support.MutableMessage
import org.springframework.messaging.Message

internal class ErrorMessagePublishingCaptorShould {
    private val repositoryService = mockk<RepositoryService>(relaxed = true)
    private val propertyServiceNameDeriver = mockk<PropertyServiceNameDeriver>()
    private val traceIdRetriever = mockk<TraceIdRetriever>()
    private val messagingHeaderRetriever = mockk<MessagingHeaderRetriever>()

    private val underTest = ErrorMessagePublishingCaptor(repositoryService, propertyServiceNameDeriver, traceIdRetriever, messagingHeaderRetriever, "profile")

    private val topic = randomAlphabetic(20)
    private val serviceName = randomAlphabetic(20)
    private val traceId = randomAlphabetic(20)
    private val body = randomAlphabetic(20)

    @Test
    fun `capture publish error interaction`() {
        every { propertyServiceNameDeriver.serviceName } returns serviceName
        every { traceIdRetriever.getTraceId(any()) } returns traceId
        every { messagingHeaderRetriever.retrieve(any()) } returns mapOf<String, Collection<String>>("name" to listOf("value"))
        val headers = mapOf("name" to listOf("value"), "Target-Name" to topic)
        val message: Message<*> = MutableMessage(body.toByteArray(), headers)
        val channel = PublishSubscribeChannel()

        val result = underTest.capturePublishErrorInteraction(message, channel)

        assertThat(result.target, `is`("unknownchannelname"))
        assertThat(result.path, `is`("unknownchannelname"))
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
