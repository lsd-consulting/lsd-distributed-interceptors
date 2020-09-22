package com.integreety.yatspec.e2e.config;

import brave.Tracer;
import com.googlecode.yatspec.state.givenwhenthen.TestState;
import com.integreety.yatspec.e2e.captor.http.RequestCaptor;
import com.integreety.yatspec.e2e.captor.http.ResponseCaptor;
import com.integreety.yatspec.e2e.captor.http.mapper.DestinationNameMappings;
import com.integreety.yatspec.e2e.captor.name.ExchangeNameDeriver;
import com.integreety.yatspec.e2e.captor.name.ServiceNameDeriver;
import com.integreety.yatspec.e2e.captor.rabbit.ConsumeCaptor;
import com.integreety.yatspec.e2e.captor.rabbit.PublishCaptor;
import com.integreety.yatspec.e2e.captor.rabbit.header.HeaderRetriever;
import com.integreety.yatspec.e2e.captor.repository.InterceptedDocumentRepository;
import com.integreety.yatspec.e2e.captor.repository.model.InterceptedCallFactory;
import com.integreety.yatspec.e2e.captor.repository.mongo.InterceptedDocumentMongoRepository;
import com.integreety.yatspec.e2e.captor.trace.TraceIdRetriever;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(name = "yatspec.lsd.db.connectionstring")
@AutoConfigureAfter(DestinationNamesAutoConfiguration.class)
@RequiredArgsConstructor
public class LibraryConfig {

    @Value("${info.app.name}")
    private String appName;

    @Value("${yatspec.lsd.db.connectionstring}")
    private String dbConnectionString;

    private final DestinationNameMappings destinationNameMappings;
    private final Tracer tracer;

    @Bean
    public TestState testState() {
        return new TestState();
    }

    @Bean
    public ServiceNameDeriver serviceNameDeriver() {
        return new ServiceNameDeriver(appName);
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
                                       final ServiceNameDeriver serviceNameDeriver) {

        return new RequestCaptor(interceptedDocumentRepository, interceptedCallFactory, serviceNameDeriver, destinationNameMappings);
    }

    @Bean
    public ResponseCaptor responseCaptor(final InterceptedDocumentRepository interceptedDocumentRepository,
                                         final InterceptedCallFactory interceptedCallFactory,
                                         final ServiceNameDeriver serviceNameDeriver) {

        return new ResponseCaptor(interceptedDocumentRepository, interceptedCallFactory, serviceNameDeriver, destinationNameMappings);
    }

    @Bean
    public InterceptedCallFactory mapGenerator(final TraceIdRetriever traceIdRetriever) {
        return new InterceptedCallFactory(traceIdRetriever);
    }

    @Bean
    public ConsumeCaptor consumeCaptor(final InterceptedDocumentRepository interceptedDocumentRepository,
                                       final InterceptedCallFactory interceptedCallFactory,
                                       final ServiceNameDeriver serviceNameDeriver,
                                       final ExchangeNameDeriver exchangeNameDeriver,
                                       final HeaderRetriever headerRetriever) {
        return new ConsumeCaptor(interceptedDocumentRepository, interceptedCallFactory, serviceNameDeriver, exchangeNameDeriver, headerRetriever);
    }

    @Bean
    public PublishCaptor publishCaptor(final InterceptedDocumentRepository interceptedDocumentRepository,
                                       final InterceptedCallFactory interceptedCallFactory,
                                       final ServiceNameDeriver serviceNameDeriver,
                                       final ExchangeNameDeriver exchangeNameDeriver,
                                       final HeaderRetriever headerRetriever) {
        return new PublishCaptor(interceptedDocumentRepository, interceptedCallFactory, serviceNameDeriver, exchangeNameDeriver, headerRetriever);
    }

    @Bean
    public InterceptedDocumentRepository interceptedDocumentRepository() {
        return new InterceptedDocumentMongoRepository(dbConnectionString);
    }
}
