package com.integreety.yatspec.e2e.config;

import com.integreety.yatspec.e2e.captor.rabbit.RabbitCaptor;
import com.integreety.yatspec.e2e.captor.rabbit.mapper.ExchangeNameDeriver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

import static com.integreety.yatspec.e2e.captor.repository.model.Type.CONSUME;

/*
    This config adds the interception of messages to RabbitMq listeners
*/
@ConditionalOnProperty(name = {"lsd.db.connectionstring"})
@ConditionalOnBean(SimpleRabbitListenerContainerFactory.class) // TODO What if there is no bean of this type?
@Configuration
@Slf4j
@RequiredArgsConstructor
public class RabbitListenerInterceptorConfig {

    private final SimpleRabbitListenerContainerFactory simpleRabbitListenerContainerFactory;
    private final RabbitCaptor rabbitCaptor;
    private final ExchangeNameDeriver exchangeNameDeriver;

    @PostConstruct
    public void postConstruct() {
        simpleRabbitListenerContainerFactory.setAfterReceivePostProcessors(this::postProcessMessage);
    }

    private Message postProcessMessage(final Message message) {
        try {
            final String exchangeName = exchangeNameDeriver.derive(message.getMessageProperties(), message.getMessageProperties().getReceivedExchange());
            rabbitCaptor.captureInteraction(exchangeName, MessageBuilder.fromMessage(message).build(), CONSUME);
        } catch (final Throwable t) {
            log.error(t.getMessage(), t);
        }
        return message;
    }
}