package io.lsdconsulting.lsd.distributed.interceptor.component

import io.lsdconsulting.lsd.distributed.connector.model.InteractionType
import io.lsdconsulting.lsd.distributed.interceptor.captor.rabbit.RabbitCaptor
import io.lsdconsulting.lsd.distributed.interceptor.captor.rabbit.mapper.deriveExchangeName
import lsd.logging.log
import org.springframework.amqp.core.Message
import org.springframework.amqp.core.MessageBuilder
import org.springframework.amqp.core.MessagePostProcessor
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.boot.autoconfigure.amqp.RabbitTemplateCustomizer
import org.springframework.stereotype.Component

@Component
class LsdRabbitTemplateCustomizer(
    private val rabbitCaptor: RabbitCaptor
) : RabbitTemplateCustomizer {
    override fun customize(rabbitTemplate: RabbitTemplate) {
        rabbitTemplate.addBeforePublishPostProcessors(MessagePostProcessor { message: Message ->
            log().info(
                "Rabbit message properties before publishing:{}",
                message.messageProperties
            )
            try {
                val exchangeName = deriveExchangeName(message.messageProperties, rabbitTemplate.exchange)
                rabbitCaptor.captureInteraction(
                    exchangeName,
                    MessageBuilder.fromMessage(message).build(),
                    InteractionType.PUBLISH
                )
            } catch (t: Throwable) {
                log().error(t.message, t)
            }
            message
        })
        rabbitTemplate.addAfterReceivePostProcessors(MessagePostProcessor { message: Message ->
            log().info(
                "Rabbit message properties after receiving:{}",
                message.messageProperties
            )
            try {
                val exchangeName = deriveExchangeName(
                    message.messageProperties,
                    message.messageProperties.receivedExchange
                )
                rabbitCaptor.captureInteraction(
                    exchangeName,
                    MessageBuilder.fromMessage(message).build(),
                    InteractionType.CONSUME
                )
            } catch (t: Throwable) {
                log().error(t.message, t)
            }
            message
        })
    }
}