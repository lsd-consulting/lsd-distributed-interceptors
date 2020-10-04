package com.integreety.yatspec.e2e.config;

import brave.Tracer;
import com.googlecode.yatspec.state.givenwhenthen.TestState;
import com.integreety.yatspec.e2e.captor.http.RequestCaptor;
import com.integreety.yatspec.e2e.captor.http.ResponseCaptor;
import com.integreety.yatspec.e2e.captor.http.mapper.PropertyServiceNameDeriver;
import com.integreety.yatspec.e2e.captor.rabbit.RabbitCaptor;
import com.integreety.yatspec.e2e.captor.rabbit.header.HeaderRetriever;
import com.integreety.yatspec.e2e.captor.rabbit.mapper.ExchangeNameDeriver;
import com.integreety.yatspec.e2e.captor.repository.InterceptedDocumentRepository;
import com.integreety.yatspec.e2e.captor.repository.model.InterceptedCallFactory;
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
    public RequestCaptor requestCaptor(final InterceptedDocumentRepository interceptedDocumentRepository,
                                       final InterceptedCallFactory interceptedCallFactory,
                                       final PropertyServiceNameDeriver propertyServiceNameDeriver) {

        return new RequestCaptor(interceptedDocumentRepository, interceptedCallFactory, propertyServiceNameDeriver);
    }

    @Bean
    public ResponseCaptor responseCaptor(final InterceptedDocumentRepository interceptedDocumentRepository,
                                         final InterceptedCallFactory interceptedCallFactory,
                                         final PropertyServiceNameDeriver propertyServiceNameDeriver) {

        return new ResponseCaptor(interceptedDocumentRepository, interceptedCallFactory, propertyServiceNameDeriver);
    }

    @Bean
    public InterceptedCallFactory mapGenerator(final TraceIdRetriever traceIdRetriever, @Value("${spring.profiles.active}") final String profile) {
        return new InterceptedCallFactory(traceIdRetriever, profile);
    }

    @Bean
    public RabbitCaptor publishCaptor(final InterceptedDocumentRepository interceptedDocumentRepository,
                                      final InterceptedCallFactory interceptedCallFactory,
                                      final HeaderRetriever headerRetriever,
                                      final PropertyServiceNameDeriver propertyServiceNameDeriver) {
        return new RabbitCaptor(interceptedDocumentRepository, interceptedCallFactory, propertyServiceNameDeriver, headerRetriever);
    }

    @Bean
    public InterceptedDocumentRepository interceptedDocumentRepository(@Value("${yatspec.lsd.db.connectionstring}") final String dbConnectionString,
                                                                       @Value("${yatspec.lsd.db.trustStoreLocation:#{null}}") final String trustStoreLocation,
                                                                       @Value("${yatspec.lsd.db.trustStorePassword:#{null}}") final String trustStorePassword) {
        return new InterceptedDocumentMongoRepository(dbConnectionString, trustStoreLocation, trustStorePassword);
    }
}
