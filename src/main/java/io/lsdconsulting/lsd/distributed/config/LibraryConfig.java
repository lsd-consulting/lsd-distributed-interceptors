package io.lsdconsulting.lsd.distributed.config;

import brave.Tracer;
import io.lsdconsulting.lsd.distributed.captor.http.RequestCaptor;
import io.lsdconsulting.lsd.distributed.captor.http.ResponseCaptor;
import io.lsdconsulting.lsd.distributed.captor.http.derive.PathDeriver;
import io.lsdconsulting.lsd.distributed.captor.http.derive.PropertyServiceNameDeriver;
import io.lsdconsulting.lsd.distributed.captor.http.derive.SourceTargetDeriver;
import io.lsdconsulting.lsd.distributed.captor.rabbit.RabbitCaptor;
import io.lsdconsulting.lsd.distributed.captor.rabbit.header.HeaderRetriever;
import io.lsdconsulting.lsd.distributed.captor.rabbit.mapper.ExchangeNameDeriver;
import io.lsdconsulting.lsd.distributed.captor.repository.InterceptedDocumentRepository;
import io.lsdconsulting.lsd.distributed.captor.repository.model.InterceptedInteractionFactory;
import io.lsdconsulting.lsd.distributed.captor.repository.mongo.InterceptedDocumentMongoRepository;
import io.lsdconsulting.lsd.distributed.captor.trace.TraceIdRetriever;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(name = "lsd.db.connectionstring")
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
    public HeaderRetriever headerRetriever() {
        return new HeaderRetriever();
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
    public SourceTargetDeriver sourceTargetDeriver(final PropertyServiceNameDeriver propertyServiceNameDeriver) {
        return new SourceTargetDeriver(propertyServiceNameDeriver);
    }

    @Bean
    public RequestCaptor requestCaptor(final InterceptedDocumentRepository interceptedDocumentRepository,
                                       final InterceptedInteractionFactory interceptedInteractionFactory,
                                       final SourceTargetDeriver sourceTargetDeriver,
                                       final PathDeriver pathDeriver,
                                       final TraceIdRetriever traceIdRetriever) {


        return new RequestCaptor(interceptedDocumentRepository, interceptedInteractionFactory, sourceTargetDeriver, pathDeriver, traceIdRetriever);
    }

    @Bean
    public ResponseCaptor responseCaptor(final InterceptedDocumentRepository interceptedDocumentRepository,
                                         final InterceptedInteractionFactory interceptedInteractionFactory,
                                         final SourceTargetDeriver sourceTargetDeriver,
                                         final PathDeriver pathDeriver,
                                         final TraceIdRetriever traceIdRetriever) {

        return new ResponseCaptor(interceptedDocumentRepository, interceptedInteractionFactory, sourceTargetDeriver, pathDeriver, traceIdRetriever);
    }

    @Bean
    public InterceptedInteractionFactory mapGenerator(@Value("${spring.profiles.active:#{''}}") final String profile) {
        return new InterceptedInteractionFactory(profile);
    }

    @Bean
    public RabbitCaptor publishCaptor(final InterceptedDocumentRepository interceptedDocumentRepository,
                                      final InterceptedInteractionFactory interceptedInteractionFactory,
                                      final HeaderRetriever headerRetriever,
                                      final PropertyServiceNameDeriver propertyServiceNameDeriver,
                                      final TraceIdRetriever traceIdRetriever) {
        return new RabbitCaptor(interceptedDocumentRepository, interceptedInteractionFactory, propertyServiceNameDeriver, headerRetriever, traceIdRetriever);
    }

    @Bean
    public InterceptedDocumentRepository interceptedDocumentRepository(@Value("${lsd.db.connectionstring}") final String dbConnectionString,
                                                                       @Value("${lsd.db.trustStoreLocation:#{null}}") final String trustStoreLocation,
                                                                       @Value("${lsd.db.trustStorePassword:#{null}}") final String trustStorePassword) {
        return new InterceptedDocumentMongoRepository(dbConnectionString, trustStoreLocation, trustStorePassword);
    }
}
