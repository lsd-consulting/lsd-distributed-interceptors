package io.lsdconsulting.lsd.distributed.interceptor.config;

import com.lsd.core.LsdContext;
import io.lsdconsulting.lsd.distributed.interceptor.captor.messaging.MessagingCaptor;
import io.lsdconsulting.lsd.distributed.interceptor.interceptor.EventConsumerInterceptor;
import io.lsdconsulting.lsd.distributed.interceptor.interceptor.EventPublisherInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.config.GlobalChannelInterceptor;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.ChannelInterceptor;

@Configuration
@ConditionalOnClass(value = {LsdContext.class, ChannelInterceptor.class, Message.class})
@ConditionalOnProperty(name = "lsd.dist.db.connectionString")
@RequiredArgsConstructor
public class MessagingConfig {

    private final MessagingCaptor messagingCaptor;

    @Bean
    @GlobalChannelInterceptor(patterns = "*-in-*", order = 100)
    public EventConsumerInterceptor eventConsumerInterceptor() {
        return new EventConsumerInterceptor(messagingCaptor);
    }

    @Bean
    @GlobalChannelInterceptor(patterns = "*-out-*", order = 101)
    public EventPublisherInterceptor eventPublisherInterceptor() {
        return new EventPublisherInterceptor(messagingCaptor);
    }
}
