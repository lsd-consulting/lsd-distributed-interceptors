package io.lsdconsulting.lsd.distributed.interceptor.config

import io.lsdconsulting.lsd.distributed.connector.model.InteractionType
import io.lsdconsulting.lsd.distributed.interceptor.captor.rabbit.RabbitCaptor
import io.lsdconsulting.lsd.distributed.interceptor.captor.rabbit.mapper.deriveExchangeName
import lsd.logging.log
import org.springframework.amqp.core.Message
import org.springframework.amqp.core.MessageBuilder
import org.springframework.amqp.core.MessagePostProcessor
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Configuration
import java.util.function.Consumer
import jakarta.annotation.PostConstruct

@Configuration
@ConditionalOnProperty(name = ["lsd.dist.connectionString"])
@ConditionalOnClass(RabbitTemplate::class)
open class RabbitTemplateInterceptorConfig(
    private val rabbitTemplates: List<RabbitTemplate>,
    private val rabbitCaptor: RabbitCaptor,
) {
    @PostConstruct
    fun configureRabbitTemplatePublishInterceptor() {
        rabbitTemplates.forEach(Consumer { rabbitTemplate: RabbitTemplate ->
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
        })
    }
}
