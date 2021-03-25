package com.integreety.yatspec.e2e.integration.testapp.config;

import com.github.fridujo.rabbitmq.mock.MockConnectionFactory;
import com.integreety.yatspec.e2e.config.mapper.ObjectMapperCreator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration
@EnableRabbit
@Slf4j
public class RabbitTemplateConfig {

    @Bean
    public CachingConnectionFactory connectionFactory() {
        return new CachingConnectionFactory(new MockConnectionFactory());
    }

    @Bean
    public RabbitTemplate rabbitTemplate() {
        final RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory());
        rabbitTemplate.setMessageConverter(new Jackson2JsonMessageConverter(new ObjectMapperCreator().getObjectMapper()));
        return rabbitTemplate;
    }
}
