package io.lsdconsulting.lsd.distributed.interceptor.config

import io.lsdconsulting.lsd.distributed.interceptor.captor.messaging.ErrorMessagePublishingCaptor
import io.lsdconsulting.lsd.distributed.interceptor.captor.messaging.MessageConsumingCaptor
import io.lsdconsulting.lsd.distributed.interceptor.captor.messaging.MessagePublishingCaptor
import io.lsdconsulting.lsd.distributed.interceptor.interceptor.ErrorChannelInterceptor
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
@ConditionalOnClass(value = [ChannelInterceptor::class, Message::class])
@ConditionalOnProperty(name = ["lsd.dist.connectionString"])
open class MessagingConfig(
    private val messageConsumingCaptor: MessageConsumingCaptor,
    private val messagePublishingCaptor: MessagePublishingCaptor,
    private val errorMessagePublishingCaptor: ErrorMessagePublishingCaptor,
) {

    @Bean
    @GlobalChannelInterceptor(patterns = ["*-in-*"], order = 100)
    open fun eventConsumerInterceptor() = InputChannelInterceptor(messageConsumingCaptor)

    @Bean
    @GlobalChannelInterceptor(patterns = ["*-out-*"], order = 101)
    open fun eventPublisherInterceptor() = OutputChannelInterceptor(messagePublishingCaptor)

    @Bean
    @GlobalChannelInterceptor(patterns = ["*errorChannel"], order = 102)
    open fun errorPublisherInterceptor() = ErrorChannelInterceptor(errorMessagePublishingCaptor)
}
