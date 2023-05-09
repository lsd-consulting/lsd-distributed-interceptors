package io.lsdconsulting.lsd.distributed.interceptor.captor.rabbit

import io.lsdconsulting.lsd.distributed.access.model.InteractionType.PUBLISH
import io.lsdconsulting.lsd.distributed.interceptor.captor.common.PropertyServiceNameDeriver
import io.lsdconsulting.lsd.distributed.interceptor.captor.trace.TraceIdRetriever
import io.lsdconsulting.lsd.distributed.interceptor.persistance.RepositoryService
import io.mockk.every
import io.mockk.mockk
import org.apache.commons.lang3.RandomStringUtils.randomAlphabetic
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers
import org.hamcrest.Matchers.emptyOrNullString
import org.hamcrest.Matchers.`is`
import org.junit.jupiter.api.Test
import org.springframework.amqp.core.Message
import org.springframework.amqp.core.MessageProperties

internal class RabbitCaptorShould {
    private val repositoryService = mockk<RepositoryService>(relaxed = true)
    private val propertyServiceNameDeriver = mockk<PropertyServiceNameDeriver>()
    private val traceIdRetriever = mockk<TraceIdRetriever>()
    private val amqpHeaderRetriever = mockk<AmqpHeaderRetriever>()

    private val underTest = RabbitCaptor(repositoryService, propertyServiceNameDeriver, traceIdRetriever, amqpHeaderRetriever, "profile")

    private val exchange = randomAlphabetic(20)
    private val serviceName = randomAlphabetic(20)
    private val traceId = randomAlphabetic(20)
    private val body = randomAlphabetic(20)
    private val messageProperties = MessageProperties()
    private val message = Message(body.toByteArray(), messageProperties)
    private val headers = mapOf<String, Collection<String>>("name" to listOf("value"))

    @Test
    fun `capture amqp interaction`() {
        every { propertyServiceNameDeriver.serviceName } returns serviceName
        every { traceIdRetriever.getTraceId(any()) } returns traceId
        every { amqpHeaderRetriever.retrieve(any()) } returns headers
        messageProperties.setHeader("name", "value")

        val (traceId1, body1, requestHeaders, responseHeaders, serviceName1, _, path, httpStatus, httpMethod, interactionType, profile) = underTest.captureInteraction(
            exchange,
            message,
            PUBLISH
        )

        assertThat(path, `is`(exchange))
        assertThat(body1, `is`(body))
        assertThat(serviceName1, `is`(serviceName))
        assertThat(traceId1, `is`(traceId))
        assertThat(interactionType, `is`(PUBLISH))
        assertThat(httpMethod, emptyOrNullString())
        assertThat(httpStatus, emptyOrNullString())
        assertThat(profile, `is`("profile"))
        assertThat(requestHeaders, `is`(headers))
        assertThat(responseHeaders, Matchers.aMapWithSize(0))
    }
}
