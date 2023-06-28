package io.lsdconsulting.lsd.distributed.interceptor.config

import com.lsd.core.LsdContext
import io.lsdconsulting.lsd.distributed.interceptor.captor.messaging.MessagingCaptor
import io.lsdconsulting.lsd.distributed.interceptor.interceptor.InputChannelInterceptor
import io.lsdconsulting.lsd.distributed.interceptor.interceptor.OutputChannelInterceptor
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.integration.config.GlobalChannelInterceptor
import org.springframework.messaging.Message
import org.springframework.messaging.support.ChannelInterceptor

@Configuration
@ConditionalOnClass(value = [LsdContext::class, ChannelInterceptor::class, Message::class])
@ConditionalOnProperty(name = ["lsd.dist.connectionString"])
open class MessagingConfig(
    private val messagingCaptor: MessagingCaptor
) {
    @Bean
    @GlobalChannelInterceptor(patterns = ["*-in-*"], order = 100)
    open fun eventConsumerInterceptor(): InputChannelInterceptor {
        return InputChannelInterceptor(messagingCaptor)
    }

    @Bean
    @GlobalChannelInterceptor(patterns = ["*-out-*"], order = 101)
    open fun eventPublisherInterceptor(): OutputChannelInterceptor {
        return OutputChannelInterceptor(messagingCaptor)
    }
}
