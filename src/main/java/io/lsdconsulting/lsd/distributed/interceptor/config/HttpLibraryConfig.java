package io.lsdconsulting.lsd.distributed.interceptor.config;

import io.lsdconsulting.lsd.distributed.interceptor.captor.common.PropertyServiceNameDeriver;
import io.lsdconsulting.lsd.distributed.interceptor.captor.header.Obfuscator;
import io.lsdconsulting.lsd.distributed.interceptor.captor.http.HttpHeaderRetriever;
import io.lsdconsulting.lsd.distributed.interceptor.captor.http.RequestCaptor;
import io.lsdconsulting.lsd.distributed.interceptor.captor.http.ResponseCaptor;
import io.lsdconsulting.lsd.distributed.interceptor.captor.http.derive.HttpStatusDeriver;
import io.lsdconsulting.lsd.distributed.interceptor.captor.http.derive.PathDeriver;
import io.lsdconsulting.lsd.distributed.interceptor.captor.http.derive.SourceTargetDeriver;
import io.lsdconsulting.lsd.distributed.interceptor.captor.trace.TraceIdRetriever;
import io.lsdconsulting.lsd.distributed.interceptor.persistance.RepositoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(name = "lsd.dist.connectionString")
@RequiredArgsConstructor
public class HttpLibraryConfig {

    @Bean
    public HttpHeaderRetriever httpHeaderRetriever(Obfuscator obfuscator) {
        return new HttpHeaderRetriever(obfuscator);
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
    public RequestCaptor requestCaptor(final RepositoryService repositoryService,
                                       final SourceTargetDeriver sourceTargetDeriver,
                                       final PathDeriver pathDeriver,
                                       final TraceIdRetriever traceIdRetriever,
                                       final HttpHeaderRetriever httpHeaderRetriever,
                                       @Value("${spring.profiles.active:#{''}}") final String profile) {


        return new RequestCaptor(repositoryService, sourceTargetDeriver,
                pathDeriver, traceIdRetriever, httpHeaderRetriever, profile);
    }

    @Bean
    public ResponseCaptor responseCaptor(final RepositoryService repositoryService,
                                         final SourceTargetDeriver sourceTargetDeriver,
                                         final PathDeriver pathDeriver,
                                         final TraceIdRetriever traceIdRetriever,
                                         final HttpHeaderRetriever httpHeaderRetriever,
                                         final HttpStatusDeriver httpStatusDeriver,
                                         @Value("${spring.profiles.active:#{''}}") final String profile) {

        return new ResponseCaptor(repositoryService, sourceTargetDeriver,
                pathDeriver, traceIdRetriever, httpHeaderRetriever, httpStatusDeriver, profile);
    }
}
