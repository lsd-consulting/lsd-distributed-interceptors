package io.lsdconsulting.lsd.distributed.interceptor.integration.testapp.config;

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
    public Exchange exchangeListener() {
        return new FanoutExchange("exchange-listener");
    }

    @Bean
    public Exchange exchangeTemplate() {
        return new FanoutExchange("exchange-rabbit-template");
    }

    @Bean
    public Queue queueListener() {
        return new Queue("queue-listener");
    }

    @Bean
    public Queue queueRabbitTemplate() {
        return new Queue("queue-rabbit-template");
    }

    @Bean
    public Binding queueListenerToExchangeBinding() {
        return new Binding("queue-listener", QUEUE, "exchange-listener", "queue-listener", null);
    }

    @Bean
    public Binding queueRabbitTemplateToExchangeBinding() {
        return new Binding("queue-rabbit-template", QUEUE, "exchange-rabbit-template", "queue-rabbit-template", null);
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
