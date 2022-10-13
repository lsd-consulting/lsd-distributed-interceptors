package io.lsdconsulting.lsd.distributed.interceptor.config;

import com.lsd.LsdContext;
import io.lsdconsulting.lsd.distributed.interceptor.captor.messaging.MessagingCaptor;
import io.lsdconsulting.lsd.distributed.interceptor.interceptor.EventConsumerInterceptor;
import io.lsdconsulting.lsd.distributed.interceptor.interceptor.EventPublisherInterceptor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.config.GlobalChannelInterceptor;
import org.springframework.messaging.support.ChannelInterceptor;

@Slf4j
@Configuration
@ConditionalOnClass(value = {LsdContext.class, ChannelInterceptor.class})
@ConditionalOnProperty(name = "lsd.dist.db.connectionString")
@RequiredArgsConstructor
public class MessagingConfig {

    private final MessagingCaptor messagingCaptor;

    @Bean
    @GlobalChannelInterceptor(patterns = "*-in-*", order = 100)
    public EventConsumerInterceptor eventConsumerInterceptor() {
        log.info("Creating EventConsumerInterceptor");
        return new EventConsumerInterceptor(messagingCaptor);
    }

    @Bean
    @GlobalChannelInterceptor(patterns = "*-out-*", order = 101)
    public EventPublisherInterceptor eventPublisherInterceptor() {
        log.info("Creating EventPublisherInterceptor");
        return new EventPublisherInterceptor(messagingCaptor);
    }
}
