package io.lsdconsulting.lsd.distributed.interceptor.integration.testapp.config

import org.springframework.amqp.core.Binding
import org.springframework.amqp.core.Exchange
import org.springframework.amqp.core.FanoutExchange
import org.springframework.amqp.core.Queue
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory
import org.springframework.boot.autoconfigure.amqp.SimpleRabbitListenerContainerFactoryConfigurer
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import java.lang.Boolean.FALSE

@TestConfiguration
class RabbitConfig(
    private val connectionFactory: CachingConnectionFactory
) {

    @Bean
    fun exchangeListener(): Exchange {
        return FanoutExchange("exchange-listener")
    }

    @Bean
    fun exchangeTemplate(): Exchange {
        return FanoutExchange("exchange-rabbit-template")
    }

    @Bean
    fun queueListener(): Queue {
        return Queue("queue-listener")
    }

    @Bean
    fun queueRabbitTemplate(): Queue {
        return Queue("queue-rabbit-template")
    }

    @Bean
    fun queueListenerToExchangeBinding(): Binding {
        return Binding("queue-listener", Binding.DestinationType.QUEUE, "exchange-listener", "queue-listener", null)
    }

    @Bean
    fun queueRabbitTemplateToExchangeBinding(): Binding {
        return Binding(
            "queue-rabbit-template",
            Binding.DestinationType.QUEUE,
            "exchange-rabbit-template",
            "queue-rabbit-template",
            null
        )
    }

    @Bean
    //     TODO This should not have to be done - investigate
    fun rabbitListenerContainerFactory(configurer: SimpleRabbitListenerContainerFactoryConfigurer): SimpleRabbitListenerContainerFactory {
        val factory = SimpleRabbitListenerContainerFactory()
        configurer.configure(factory, connectionFactory)
        factory.setDefaultRequeueRejected(FALSE)
        factory.setMissingQueuesFatal(false)
        factory.setFailedDeclarationRetryInterval(15000L)
        return factory
    }
}
