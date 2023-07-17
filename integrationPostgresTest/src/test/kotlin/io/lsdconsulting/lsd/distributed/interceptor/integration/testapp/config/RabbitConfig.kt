package io.lsdconsulting.lsd.distributed.interceptor.integration.testapp.config

import org.springframework.amqp.core.Binding
import org.springframework.amqp.core.FanoutExchange
import org.springframework.amqp.core.Queue
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory
import org.springframework.boot.autoconfigure.amqp.SimpleRabbitListenerContainerFactoryConfigurer
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import java.lang.Boolean.FALSE

@TestConfiguration
open class RabbitConfig(
    private val connectionFactory: CachingConnectionFactory
) {

    @Bean
    open fun exchangeListener() = FanoutExchange("exchange-listener")

    @Bean
    open fun exchangeTemplate() = FanoutExchange("exchange-rabbit-template")

    @Bean
    open fun queueListener() = Queue("queue-listener")

    @Bean
    open fun queueRabbitTemplate() = Queue("queue-rabbit-template")

    @Bean
    open fun queueListenerToExchangeBinding() =
        Binding("queue-listener", Binding.DestinationType.QUEUE, "exchange-listener", "queue-listener", null)

    @Bean
    open fun queueRabbitTemplateToExchangeBinding() = Binding(
        "queue-rabbit-template",
        Binding.DestinationType.QUEUE,
        "exchange-rabbit-template",
        "queue-rabbit-template",
        null
    )

    @Bean
    //     TODO This should not have to be done - investigate
    open fun rabbitListenerContainerFactory(configurer: SimpleRabbitListenerContainerFactoryConfigurer): SimpleRabbitListenerContainerFactory {
        val factory = SimpleRabbitListenerContainerFactory()
        configurer.configure(factory, connectionFactory)
        factory.setDefaultRequeueRejected(FALSE)
        factory.setMissingQueuesFatal(false)
        factory.setFailedDeclarationRetryInterval(15000L)
        return factory
    }
}
