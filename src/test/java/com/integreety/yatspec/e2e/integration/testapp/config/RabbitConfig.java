package com.integreety.yatspec.e2e.integration.testapp.config;

import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.boot.autoconfigure.amqp.SimpleRabbitListenerContainerFactoryConfigurer;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import static java.lang.Boolean.FALSE;
import static org.springframework.amqp.core.Binding.DestinationType.QUEUE;

@TestConfiguration
@RequiredArgsConstructor
public class RabbitConfig {

    private final CachingConnectionFactory connectionFactory;

    @Bean
    public Exchange exchange() {
        return new FanoutExchange("exchange");
    }

    @Bean
    public Queue queue() {
        return new Queue("queue");
    }

    @Bean
    public Binding binding() {
        return new Binding("queue", QUEUE, "exchange", "queue", null);
    }

    @Bean
//     TODO This should not have to be done - investigate
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(final SimpleRabbitListenerContainerFactoryConfigurer configurer) {
        final SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        configurer.configure(factory, connectionFactory);
        factory.setDefaultRequeueRejected(FALSE);
        factory.setMissingQueuesFatal(false);
        factory.setFailedDeclarationRetryInterval(15_000L);
        return factory;
    }
}
