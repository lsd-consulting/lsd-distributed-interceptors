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
open class InputRabbitMqConfig(
    @Value("\${spring.cloud.stream.bindings.inputOutputHandlerFunction-in-0.destination}")
    private val inputExchange: String,
    @Value("\${spring.cloud.stream.bindings.noOutputLsdHeadersHandlerFunction-in-0.destination}")
    private val noLsdHeadersInputExchange: String,
    @Value("\${spring.cloud.stream.bindings.inputOutputHandlerFunction-in-0.group}")
    private val inputQueue: String,
    @Value("\${spring.cloud.stream.bindings.noOutputLsdHeadersHandlerFunction-in-0.group}")
    private val noLsdHeadersInputQueue: String,
) {
    @Bean
    open fun connectionFactory(): CachingConnectionFactory = CachingConnectionFactory(MockConnectionFactory())

    @Bean
    open fun inputExchange() = FanoutExchange(inputExchange)

    @Bean
    open fun noLsdHeadersInputExchange() = FanoutExchange(noLsdHeadersInputExchange)

    @Bean
    open fun inputQueue(): Queue = Queue(inputQueue)

    @Bean
    open fun noLsdHeadersInputQueue(): Queue = Queue(noLsdHeadersInputQueue)

    @Bean
    open fun bindInputQueue(
        inputQueue: Queue,
        inputExchange: FanoutExchange
    ): Binding {
        return BindingBuilder.bind(inputQueue).to(inputExchange)
    }

    @Bean
    open fun bindNoLsdHeadersInputQueue(
        noLsdHeadersInputQueue: Queue,
        noLsdHeadersInputExchange: FanoutExchange
    ): Binding {
        return BindingBuilder.bind(noLsdHeadersInputQueue).to(noLsdHeadersInputExchange)
    }
}
