package com.integreety.yatspec.e2e.config;

import com.integreety.yatspec.e2e.captor.rabbit.ConsumeCaptor;
import com.integreety.yatspec.e2e.captor.rabbit.PublishCaptor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.util.List;

@Slf4j
@Configuration
@ConditionalOnProperty(name = {"yatspec.lsd.db.connectionstring"})
@ConditionalOnClass(RabbitTemplate.class)
@RequiredArgsConstructor
public class RabbitTemplateInterceptorConfig {

    private final List<RabbitTemplate> rabbitTemplates;
    private final PublishCaptor publishCaptor;
    private final ConsumeCaptor consumeCaptor;

    @PostConstruct
    public void configureRabbitTemplatePublishInterceptor() {
        rabbitTemplates.forEach(rabbitTemplate -> {
            rabbitTemplate.addBeforePublishPostProcessors(message -> {
                publishCaptor.capturePublishInteraction(rabbitTemplate.getExchange(), MessageBuilder.fromMessage(message).build());
                return message;
            });
            rabbitTemplate.addAfterReceivePostProcessors(message -> {
                consumeCaptor.captureConsumeInteraction(rabbitTemplate.getExchange(), MessageBuilder.fromMessage(message).build());
                return message;
            });
        });
    }
}