package io.lsdconsulting.lsd.distributed.interceptor.config;

import brave.Tracer;
import io.lsdconsulting.lsd.distributed.access.repository.InterceptedDocumentRepository;
import io.lsdconsulting.lsd.distributed.interceptor.captor.common.PropertyServiceNameDeriver;
import io.lsdconsulting.lsd.distributed.interceptor.captor.header.Obfuscator;
import io.lsdconsulting.lsd.distributed.interceptor.captor.trace.TraceIdRetriever;
import io.lsdconsulting.lsd.distributed.interceptor.persistance.QueueService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(name = "lsd.dist.db.connectionString")
@RequiredArgsConstructor
public class CommonLibraryConfig {

    private final Tracer tracer;

    @Bean
    public PropertyServiceNameDeriver propertyServiceNameDeriver(@Value("${info.app.name}") final String appName) {
        return new PropertyServiceNameDeriver(appName);
    }

    @Bean
    public Obfuscator obfuscator(@Value("${lsd.dist.obfuscator.sensitiveHeaders:#{null}}") final String sensitiveHeaders) {
        return new Obfuscator(sensitiveHeaders);
    }

    @Bean
    public TraceIdRetriever traceIdRetriever() {
        return new TraceIdRetriever(tracer);
    }

    @Bean
    public QueueService queueService(@Value("${lsd.dist.queue.length:16}") final int queueLength,
                                     InterceptedDocumentRepository interceptedDocumentRepository) {
        return new QueueService(queueLength, interceptedDocumentRepository);
    }
}
