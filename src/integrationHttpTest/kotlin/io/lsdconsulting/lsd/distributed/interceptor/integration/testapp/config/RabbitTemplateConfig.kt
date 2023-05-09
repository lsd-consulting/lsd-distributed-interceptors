package io.lsdconsulting.lsd.distributed.interceptor.integration.testapp.config

import com.github.fridujo.rabbitmq.mock.MockConnectionFactory
import io.lsdconsulting.lsd.distributed.interceptor.config.mapper.ObjectMapperCreator
import org.springframework.amqp.rabbit.annotation.EnableRabbit
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean

@TestConfiguration
@EnableRabbit
open class RabbitTemplateConfig {
    @Bean
    open fun connectionFactory() = CachingConnectionFactory(MockConnectionFactory())

    @Bean
    open fun rabbitTemplate(): RabbitTemplate {
        val rabbitTemplate = RabbitTemplate(connectionFactory())
        rabbitTemplate.messageConverter =
            Jackson2JsonMessageConverter(ObjectMapperCreator().objectMapper)
        return rabbitTemplate
    }
}
