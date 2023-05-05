package io.lsdconsulting.lsd.distributed.interceptor.config

import io.lsdconsulting.lsd.distributed.access.model.InteractionType
import io.lsdconsulting.lsd.distributed.interceptor.captor.rabbit.RabbitCaptor
import io.lsdconsulting.lsd.distributed.interceptor.captor.rabbit.mapper.ExchangeNameDeriver
import org.springframework.amqp.core.Message
import org.springframework.amqp.core.MessageBuilder
import org.springframework.amqp.core.MessagePostProcessor
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Configuration
import javax.annotation.PostConstruct

/*
    This config adds the interception of messages to RabbitMq listeners
*/
@ConditionalOnProperty(name = ["lsd.dist.connectionString"])
@ConditionalOnBean(SimpleRabbitListenerContainerFactory::class) // TODO What if there is no bean of this type?
@Configuration
open class RabbitListenerInterceptorConfig(
    private val simpleRabbitListenerContainerFactory: SimpleRabbitListenerContainerFactory,
    private val rabbitCaptor: RabbitCaptor,
    private val exchangeNameDeriver: ExchangeNameDeriver,
) {

    @PostConstruct
    fun postConstruct() {
        simpleRabbitListenerContainerFactory.setAfterReceivePostProcessors(MessagePostProcessor { message: Message ->
            postProcessMessage(
                message
            )
        })
    }

    private fun postProcessMessage(message: Message): Message {
        try {
            val exchangeName =
                exchangeNameDeriver.derive(message.messageProperties, message.messageProperties.receivedExchange)
            rabbitCaptor.captureInteraction(
                exchangeName,
                MessageBuilder.fromMessage(message).build(),
                InteractionType.CONSUME
            )
        } catch (t: Throwable) {
            log().error(t.message, t)
        }
        return message
    }
}
