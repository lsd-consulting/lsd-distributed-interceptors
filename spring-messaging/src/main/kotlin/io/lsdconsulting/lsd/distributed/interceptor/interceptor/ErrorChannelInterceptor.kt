package io.lsdconsulting.lsd.distributed.interceptor.interceptor

import io.lsdconsulting.lsd.distributed.interceptor.captor.messaging.ErrorMessagePublishingCaptor
import org.springframework.messaging.Message
import org.springframework.messaging.MessageChannel
import org.springframework.messaging.support.ChannelInterceptor


class ErrorChannelInterceptor(private val captor: ErrorMessagePublishingCaptor) : ChannelInterceptor {
    override fun preSend(message: Message<*>, channel: MessageChannel): Message<*> {
        captor.capturePublishErrorInteraction(message, channel)
        return message
    }
}
