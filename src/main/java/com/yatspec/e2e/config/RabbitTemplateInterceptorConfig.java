package com.yatspec.e2e.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.yatspec.e2e.captor.rabbit.ConsumeCaptor;
import com.yatspec.e2e.captor.rabbit.PublishCaptor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

@Slf4j
@Configuration
@ConditionalOnProperty(name = {"yatspec.lsd.db.connectionstring"})
@ConditionalOnClass(RabbitTemplate.class)
@RequiredArgsConstructor
public class RabbitTemplateInterceptorConfig {

    private final RabbitTemplate rabbitTemplate;
    private final PublishCaptor publishCaptor;
    private final ConsumeCaptor consumeCaptor;

    @PostConstruct
    public void configureRabbitTemplatePublishInterceptor() {
        rabbitTemplate.addBeforePublishPostProcessors(message -> {
            try {
                publishCaptor.capturePublishInteraction(MessageBuilder.fromMessage(message).build());
            } catch (final JsonProcessingException e) {
                log.error(e.getMessage(), e);
            }
            return message;
        });
        rabbitTemplate.addAfterReceivePostProcessors(message -> {
            try {
                consumeCaptor.captureConsumeInteraction(MessageBuilder.fromMessage(message).build());
            } catch (final JsonProcessingException e) {
                log.error(e.getMessage(), e);
            }
            return message;
        });
    }
}