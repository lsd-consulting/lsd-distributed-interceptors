package io.lsdconsulting.lsd.distributed.interceptor.interceptor

import io.lsdconsulting.lsd.distributed.interceptor.captor.messaging.MessagePublishingCaptor
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.jupiter.api.Test
import org.springframework.integration.channel.AbstractMessageChannel
import org.springframework.messaging.Message

internal class OutputChannelInterceptorShould {

    private val message = mockk<Message<String>>(relaxed = true)
    private val messagingCaptor = mockk<MessagePublishingCaptor>(relaxed = true)
    private val channel = mockk<AbstractMessageChannel>()
    private val underTest = OutputChannelInterceptor(messagingCaptor)

    @Test
    fun `return same message`() {
        every { channel.fullChannelName } returns randomAlphanumeric(10)

        val result = underTest.preSend(message, channel)

        assertThat(result, `is`(message))
    }

    @Test
    fun `capture message`() {
        val channelName = randomAlphanumeric(10)
        every { channel.fullChannelName } returns channelName

        underTest.preSend(message, channel)

        verify { messagingCaptor.capturePublishInteraction(message, channelName) }
    }
}
