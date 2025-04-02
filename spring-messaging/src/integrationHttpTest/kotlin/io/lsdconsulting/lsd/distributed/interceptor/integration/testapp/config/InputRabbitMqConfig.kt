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
class InputRabbitMqConfig(
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
    fun connectionFactory(): CachingConnectionFactory = CachingConnectionFactory(MockConnectionFactory())

    @Bean
    fun inputExchange() = FanoutExchange(inputExchange)

    @Bean
    fun noLsdHeadersInputExchange() = FanoutExchange(noLsdHeadersInputExchange)

    @Bean
    fun inputQueue(): Queue = Queue(inputQueue)

    @Bean
    fun noLsdHeadersInputQueue(): Queue = Queue(noLsdHeadersInputQueue)

    @Bean
    fun bindInputQueue(
        inputQueue: Queue,
        inputExchange: FanoutExchange
    ): Binding {
        return BindingBuilder.bind(inputQueue).to(inputExchange)
    }

    @Bean
    fun bindNoLsdHeadersInputQueue(
        noLsdHeadersInputQueue: Queue,
        noLsdHeadersInputExchange: FanoutExchange
    ): Binding {
        return BindingBuilder.bind(noLsdHeadersInputQueue).to(noLsdHeadersInputExchange)
    }
}
