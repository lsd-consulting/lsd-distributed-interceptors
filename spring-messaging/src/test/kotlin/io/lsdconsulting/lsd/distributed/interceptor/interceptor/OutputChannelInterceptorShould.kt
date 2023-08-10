package io.lsdconsulting.lsd.distributed.interceptor.interceptor

import io.lsdconsulting.lsd.distributed.interceptor.captor.messaging.MessagePublishingCaptor
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.jupiter.api.Test
import org.springframework.integration.channel.AbstractMessageChannel
import org.springframework.messaging.Message

internal class OutputChannelInterceptorShould {

    private val message = mockk<Message<String>>(relaxed = true)
    private val messagingCaptor = mockk<MessagePublishingCaptor>(relaxed = true)
    private val underTest = OutputChannelInterceptor(messagingCaptor)

    @Test
    fun `return same message`() {
        val result = underTest.preSend(message, mockk())
        assertThat(result, `is`(message))
    }

    @Test
    fun `capture message`() {
        val channel = mockk<AbstractMessageChannel>()
        every { channel.fullChannelName } returns "fullChannelName"
        underTest.preSend(message, channel)
        verify { messagingCaptor.capturePublishInteraction(message, "fullChannelName") }
    }
}
