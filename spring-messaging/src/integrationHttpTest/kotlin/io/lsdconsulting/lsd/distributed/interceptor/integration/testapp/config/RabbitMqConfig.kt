package io.lsdconsulting.lsd.distributed.interceptor.integration.testapp.config

import com.github.fridujo.rabbitmq.mock.MockConnectionFactory
import org.springframework.amqp.core.Binding
import org.springframework.amqp.core.BindingBuilder
import org.springframework.amqp.core.FanoutExchange
import org.springframework.amqp.core.Queue
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean

@TestConfiguration
open class RabbitMqConfig(
    @Value("\${spring.cloud.stream.bindings.inputOutputHandlerFunction-in-0.destination}")
    private val inputExchange: String,
) {
    @Bean
    open fun connectionFactory(): CachingConnectionFactory = CachingConnectionFactory(MockConnectionFactory())

    @Bean
    open fun inputExchange() = FanoutExchange(inputExchange)

    @Bean
    open fun inputQueue(): Queue = Queue("input.queue")

    @Bean
    open fun bindOrderUpdatedEventQueue(
        outputQueue: Queue,
        outputExchange: FanoutExchange
    ): Binding {
        return BindingBuilder.bind(outputQueue).to(outputExchange)
    }
}
