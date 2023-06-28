package io.lsdconsulting.lsd.distributed.interceptor.config

import io.lsdconsulting.lsd.distributed.interceptor.captor.HttpHeaderRetriever
import io.lsdconsulting.lsd.distributed.interceptor.captor.RequestCaptor
import io.lsdconsulting.lsd.distributed.interceptor.captor.ResponseCaptor
import io.lsdconsulting.lsd.distributed.interceptor.captor.header.Obfuscator
import io.lsdconsulting.lsd.distributed.interceptor.captor.http.derive.SourceTargetDeriver
import io.lsdconsulting.lsd.distributed.interceptor.captor.trace.TraceIdRetriever
import io.lsdconsulting.lsd.distributed.interceptor.persistance.RepositoryService
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
@ConditionalOnProperty(name = ["lsd.dist.connectionString"])
open class HttpLibraryConfig {
    @Bean
    open fun httpHeaderRetriever(obfuscator: Obfuscator) = HttpHeaderRetriever(obfuscator)

    @Bean
    open fun requestCaptor(
        repositoryService: RepositoryService,
        sourceTargetDeriver: SourceTargetDeriver,
        traceIdRetriever: TraceIdRetriever,
        httpHeaderRetriever: HttpHeaderRetriever,
        @Value("\${spring.profiles.active:#{''}}") profile: String
    ) = RequestCaptor(repositoryService, sourceTargetDeriver, traceIdRetriever, httpHeaderRetriever, profile)

    @Bean
    open fun responseCaptor(
        repositoryService: RepositoryService,
        sourceTargetDeriver: SourceTargetDeriver,
        httpHeaderRetriever: HttpHeaderRetriever,
        @Value("\${spring.profiles.active:#{''}}") profile: String
    ) = ResponseCaptor(repositoryService, sourceTargetDeriver, httpHeaderRetriever, profile)
}
