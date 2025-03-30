package io.lsdconsulting.lsd.distributed.interceptor.config

import io.lsdconsulting.lsd.distributed.interceptor.captor.HttpHeaderRetriever
import io.lsdconsulting.lsd.distributed.interceptor.captor.RequestCaptor
import io.lsdconsulting.lsd.distributed.interceptor.captor.ResponseCaptor
import io.lsdconsulting.lsd.distributed.interceptor.captor.common.Obfuscator
import io.lsdconsulting.lsd.distributed.interceptor.captor.http.SourceTargetDeriver
import io.lsdconsulting.lsd.distributed.interceptor.captor.trace.TraceIdRetriever
import io.lsdconsulting.lsd.distributed.interceptor.persistence.RepositoryService
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
@ConditionalOnProperty(name = ["lsd.dist.connectionString"])
class HttpLibraryConfig {
    @Bean
    fun httpHeaderRetriever(obfuscator: Obfuscator) = HttpHeaderRetriever(obfuscator)

    @Bean
    fun requestCaptor(
        repositoryService: RepositoryService,
        sourceTargetDeriver: SourceTargetDeriver,
        traceIdRetriever: TraceIdRetriever,
        httpHeaderRetriever: HttpHeaderRetriever,
        @Value("\${spring.profiles.active:#{''}}") profile: String
    ) = RequestCaptor(repositoryService, sourceTargetDeriver, traceIdRetriever, httpHeaderRetriever, profile)

    @Bean
    fun responseCaptor(
        repositoryService: RepositoryService,
        sourceTargetDeriver: SourceTargetDeriver,
        httpHeaderRetriever: HttpHeaderRetriever,
        @Value("\${spring.profiles.active:#{''}}") profile: String
    ) = ResponseCaptor(repositoryService, sourceTargetDeriver, httpHeaderRetriever, profile)
}
