package com.integreety.yatspec.e2e.config;

import brave.Tracer;
import com.googlecode.yatspec.state.givenwhenthen.TestState;
import com.integreety.yatspec.e2e.captor.http.RequestCaptor;
import com.integreety.yatspec.e2e.captor.http.ResponseCaptor;
import com.integreety.yatspec.e2e.captor.http.derive.PathDeriver;
import com.integreety.yatspec.e2e.captor.http.derive.PropertyServiceNameDeriver;
import com.integreety.yatspec.e2e.captor.http.derive.SourceTargetDeriver;
import com.integreety.yatspec.e2e.captor.rabbit.RabbitCaptor;
import com.integreety.yatspec.e2e.captor.rabbit.header.HeaderRetriever;
import com.integreety.yatspec.e2e.captor.rabbit.mapper.ExchangeNameDeriver;
import com.integreety.yatspec.e2e.captor.repository.InterceptedDocumentRepository;
import com.integreety.yatspec.e2e.captor.repository.model.InterceptedInteractionFactory;
import com.integreety.yatspec.e2e.captor.repository.mongo.InterceptedDocumentMongoRepository;
import com.integreety.yatspec.e2e.captor.trace.TraceIdRetriever;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(name = "yatspec.lsd.db.connectionstring")
@RequiredArgsConstructor
public class LibraryConfig {

    private final Tracer tracer;

    @Bean
    public TestState testState() {
        return new TestState();
    }

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
    public InterceptedDocumentRepository interceptedDocumentRepository(@Value("${yatspec.lsd.db.connectionstring}") final String dbConnectionString,
                                                                       @Value("${yatspec.lsd.db.trustStoreLocation:#{null}}") final String trustStoreLocation,
                                                                       @Value("${yatspec.lsd.db.trustStorePassword:#{null}}") final String trustStorePassword) {
        return new InterceptedDocumentMongoRepository(dbConnectionString, trustStoreLocation, trustStorePassword);
    }
}
