package io.lsdconsulting.lsd.distributed.interceptor.interceptor

import io.lsdconsulting.lsd.distributed.interceptor.captor.messaging.MessagingCaptor
import io.mockk.mockk
import io.mockk.verify
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.jupiter.api.Test
import org.springframework.messaging.Message

internal class InputChannelInterceptorShould {

    private val message = mockk<Message<String>>(relaxed = true)
    private val messagingCaptor = mockk< MessagingCaptor>(relaxed = true)
    private val underTest = InputChannelInterceptor(messagingCaptor)

    @Test
    fun `return same message`() {
        val result = underTest.preSend(message, mockk())
        assertThat(result, `is`(message))
    }

    @Test
    fun `capture message`() {
        underTest.preSend(message, mockk())
        verify { messagingCaptor.captureConsumeInteraction(message) }
    }
}
