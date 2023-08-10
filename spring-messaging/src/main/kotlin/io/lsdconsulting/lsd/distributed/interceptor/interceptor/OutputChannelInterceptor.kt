package io.lsdconsulting.lsd.distributed.interceptor.interceptor

import io.lsdconsulting.lsd.distributed.interceptor.captor.messaging.MessagePublishingCaptor
import org.springframework.integration.channel.AbstractMessageChannel
import org.springframework.messaging.Message
import org.springframework.messaging.MessageChannel
import org.springframework.messaging.support.ChannelInterceptor

class OutputChannelInterceptor(private val captor: MessagePublishingCaptor) : ChannelInterceptor {
    override fun preSend(message: Message<*>, channel: MessageChannel): Message<*> {
        captor.capturePublishInteraction(message, (channel as AbstractMessageChannel).fullChannelName)
        return message
    }
}
