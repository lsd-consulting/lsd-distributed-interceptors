package io.lsdconsulting.lsd.distributed.interceptor.config;

import brave.Tracer;
import io.lsdconsulting.lsd.distributed.access.repository.InterceptedDocumentRepository;
import io.lsdconsulting.lsd.distributed.interceptor.captor.common.PropertyServiceNameDeriver;
import io.lsdconsulting.lsd.distributed.interceptor.captor.header.Obfuscator;
import io.lsdconsulting.lsd.distributed.interceptor.captor.rabbit.AmqpHeaderRetriever;
import io.lsdconsulting.lsd.distributed.interceptor.captor.rabbit.RabbitCaptor;
import io.lsdconsulting.lsd.distributed.interceptor.captor.rabbit.mapper.ExchangeNameDeriver;
import io.lsdconsulting.lsd.distributed.interceptor.captor.trace.TraceIdRetriever;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(name = "lsd.dist.db.connectionString")
@RequiredArgsConstructor
public class AmqpLibraryConfig {

    private final Tracer tracer;

    @Bean
    @ConditionalOnClass(name = "org.springframework.amqp.core.MessageProperties")
    public ExchangeNameDeriver exchangeNameDeriver() {
        return new ExchangeNameDeriver();
    }

    @Bean
    @ConditionalOnClass(name = "org.springframework.amqp.core.Message")
    public AmqpHeaderRetriever amqpHeaderRetriever(Obfuscator obfuscator) {
        return new AmqpHeaderRetriever(obfuscator);
    }

    @Bean
    @ConditionalOnBean(name = "amqpHeaderRetriever")
    public RabbitCaptor publishCaptor(final InterceptedDocumentRepository interceptedDocumentRepository,
                                      final PropertyServiceNameDeriver propertyServiceNameDeriver,
                                      final TraceIdRetriever traceIdRetriever,
                                      final AmqpHeaderRetriever amqpHeaderRetriever,
                                      @Value("${spring.profiles.active:#{''}}") final String profile) {

        return new RabbitCaptor(interceptedDocumentRepository, propertyServiceNameDeriver, traceIdRetriever, amqpHeaderRetriever, profile);
    }
}
