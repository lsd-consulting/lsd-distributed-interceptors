package io.lsdconsulting.lsd.distributed.interceptor.config;

import io.lsdconsulting.lsd.distributed.interceptor.captor.common.PropertyServiceNameDeriver;
import io.lsdconsulting.lsd.distributed.interceptor.captor.header.Obfuscator;
import io.lsdconsulting.lsd.distributed.interceptor.captor.messaging.MessagingCaptor;
import io.lsdconsulting.lsd.distributed.interceptor.captor.messaging.MessagingHeaderRetriever;
import io.lsdconsulting.lsd.distributed.interceptor.captor.trace.TraceIdRetriever;
import io.lsdconsulting.lsd.distributed.interceptor.persistance.QueueService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(name = "lsd.dist.db.connectionString")
@ConditionalOnClass(org.springframework.messaging.Message.class)
@RequiredArgsConstructor
public class MessagingLibraryConfig {

    @Bean
    public MessagingHeaderRetriever messagingHeaderRetriever(Obfuscator obfuscator) {
        return new MessagingHeaderRetriever(obfuscator);
    }

    @Bean
    public MessagingCaptor messagingCaptor(final QueueService queueService,
                                           final PropertyServiceNameDeriver propertyServiceNameDeriver,
                                           final TraceIdRetriever traceIdRetriever,
                                           final MessagingHeaderRetriever messagingHeaderRetriever,
                                           @Value("${spring.profiles.active:#{''}}") final String profile) {
        return new MessagingCaptor(queueService, propertyServiceNameDeriver, traceIdRetriever, messagingHeaderRetriever, profile);
    }
}
