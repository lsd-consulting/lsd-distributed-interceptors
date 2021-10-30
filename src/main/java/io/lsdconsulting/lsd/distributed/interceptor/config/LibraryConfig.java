package io.lsdconsulting.lsd.distributed.interceptor.config;

import brave.Tracer;
import io.lsdconsulting.lsd.distributed.access.repository.InterceptedDocumentRepository;
import io.lsdconsulting.lsd.distributed.interceptor.captor.header.HeaderRetriever;
import io.lsdconsulting.lsd.distributed.interceptor.captor.header.Obfuscator;
import io.lsdconsulting.lsd.distributed.interceptor.captor.http.RequestCaptor;
import io.lsdconsulting.lsd.distributed.interceptor.captor.http.ResponseCaptor;
import io.lsdconsulting.lsd.distributed.interceptor.captor.http.derive.HttpStatusDeriver;
import io.lsdconsulting.lsd.distributed.interceptor.captor.http.derive.PathDeriver;
import io.lsdconsulting.lsd.distributed.interceptor.captor.http.derive.PropertyServiceNameDeriver;
import io.lsdconsulting.lsd.distributed.interceptor.captor.http.derive.SourceTargetDeriver;
import io.lsdconsulting.lsd.distributed.interceptor.captor.rabbit.RabbitCaptor;
import io.lsdconsulting.lsd.distributed.interceptor.captor.rabbit.mapper.ExchangeNameDeriver;
import io.lsdconsulting.lsd.distributed.interceptor.captor.trace.TraceIdRetriever;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(name = "lsd.dist.db.connectionString")
@RequiredArgsConstructor
public class LibraryConfig {

    private final Tracer tracer;

    @Bean
    public PropertyServiceNameDeriver propertyServiceNameDeriver(@Value("${info.app.name}") final String appName) {
        return new PropertyServiceNameDeriver(appName);
    }

    @Bean
    public ExchangeNameDeriver exchangeNameDeriver() {
        return new ExchangeNameDeriver();
    }

    @Bean
    public Obfuscator obfuscator(@Value("${lsd.dist.obfuscator.sensitiveHeaders:#{null}}") final String sensitiveHeaders) {
        return new Obfuscator(sensitiveHeaders);
    }

    @Bean
    public HeaderRetriever headerRetriever(Obfuscator obfuscator) {
        return new HeaderRetriever(obfuscator);
    }

    @Bean
    public TraceIdRetriever traceIdRetriever() {
        return new TraceIdRetriever(tracer);
    }

    @Bean
    public PathDeriver pathDeriver() {
        return new PathDeriver();
    }

    @Bean
    public HttpStatusDeriver httpStatusDeriver() {
        return new HttpStatusDeriver();
    }


    @Bean
    public SourceTargetDeriver sourceTargetDeriver(final PropertyServiceNameDeriver propertyServiceNameDeriver) {
        return new SourceTargetDeriver(propertyServiceNameDeriver);
    }

    @Bean
    public RequestCaptor requestCaptor(final InterceptedDocumentRepository interceptedDocumentRepository,
                                       final SourceTargetDeriver sourceTargetDeriver,
                                       final PathDeriver pathDeriver,
                                       final TraceIdRetriever traceIdRetriever,
                                       final HeaderRetriever headerRetriever,
                                       @Value("${spring.profiles.active:#{''}}") final String profile) {


        return new RequestCaptor(interceptedDocumentRepository, sourceTargetDeriver,
                pathDeriver, traceIdRetriever, headerRetriever, profile);
    }

    @Bean
    public ResponseCaptor responseCaptor(final InterceptedDocumentRepository interceptedDocumentRepository,
                                         final SourceTargetDeriver sourceTargetDeriver,
                                         final PathDeriver pathDeriver,
                                         final TraceIdRetriever traceIdRetriever,
                                         final HeaderRetriever headerRetriever,
                                         final HttpStatusDeriver httpStatusDeriver,
                                         @Value("${spring.profiles.active:#{''}}") final String profile) {

        return new ResponseCaptor(interceptedDocumentRepository, sourceTargetDeriver,
                pathDeriver, traceIdRetriever, headerRetriever, httpStatusDeriver, profile);
    }

    @Bean
    public RabbitCaptor publishCaptor(final InterceptedDocumentRepository interceptedDocumentRepository,
                                      final PropertyServiceNameDeriver propertyServiceNameDeriver,
                                      final TraceIdRetriever traceIdRetriever,
                                      final HeaderRetriever headerRetriever,
                                      @Value("${spring.profiles.active:#{''}}") final String profile) {

        return new RabbitCaptor(interceptedDocumentRepository, propertyServiceNameDeriver, traceIdRetriever, headerRetriever, profile);
    }
}
