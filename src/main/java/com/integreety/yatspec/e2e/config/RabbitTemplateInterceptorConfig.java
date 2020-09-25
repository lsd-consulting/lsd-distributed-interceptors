package com.integreety.yatspec.e2e.config;

import com.integreety.yatspec.e2e.captor.rabbit.RabbitCaptor;
import com.integreety.yatspec.e2e.captor.rabbit.mapper.ExchangeNameDeriver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.util.List;

import static com.integreety.yatspec.e2e.captor.repository.model.Type.CONSUME;
import static com.integreety.yatspec.e2e.captor.repository.model.Type.PUBLISH;

@Slf4j
@Configuration
@ConditionalOnProperty(name = {"yatspec.lsd.db.connectionstring"})
@ConditionalOnClass(RabbitTemplate.class)
@RequiredArgsConstructor
public class RabbitTemplateInterceptorConfig {

    private final List<RabbitTemplate> rabbitTemplates;
    private final RabbitCaptor rabbitCaptor;
    private final ExchangeNameDeriver exchangeNameDeriver;

    @PostConstruct
    public void configureRabbitTemplatePublishInterceptor() {
        rabbitTemplates.forEach(rabbitTemplate -> {
            rabbitTemplate.addBeforePublishPostProcessors(message -> {
                log.info("Rabbit message properties before publishing:{}", message.getMessageProperties());
                final String exchangeName = exchangeNameDeriver.derive(message.getMessageProperties(), rabbitTemplate.getExchange());
                rabbitCaptor.captureInteraction(exchangeName, MessageBuilder.fromMessage(message).build(), PUBLISH);
                return message;
            });
            rabbitTemplate.addAfterReceivePostProcessors(message -> {
                log.info("Rabbit message properties after receiving:{}", message.getMessageProperties());
                final String exchangeName = exchangeNameDeriver.derive(message.getMessageProperties(), message.getMessageProperties().getReceivedExchange());
                rabbitCaptor.captureInteraction(exchangeName, MessageBuilder.fromMessage(message).build(), CONSUME);
                return message;
            });
        });
    }
}