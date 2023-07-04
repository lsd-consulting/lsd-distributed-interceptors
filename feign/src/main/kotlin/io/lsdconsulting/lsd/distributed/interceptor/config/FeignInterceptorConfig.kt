package io.lsdconsulting.lsd.distributed.interceptor.config

import feign.Logger.Level
import io.lsdconsulting.lsd.distributed.interceptor.captor.FeignHttpHeaderRetriever
import io.lsdconsulting.lsd.distributed.interceptor.captor.FeignRequestCaptor
import io.lsdconsulting.lsd.distributed.interceptor.captor.FeignResponseCaptor
import io.lsdconsulting.lsd.distributed.interceptor.captor.common.Obfuscator
import io.lsdconsulting.lsd.distributed.interceptor.captor.http.SourceTargetDeriver
import io.lsdconsulting.lsd.distributed.interceptor.captor.trace.TraceIdRetriever
import io.lsdconsulting.lsd.distributed.interceptor.interceptor.LsdFeignLoggerInterceptor
import io.lsdconsulting.lsd.distributed.interceptor.persistance.RepositoryService
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
@ConditionalOnProperty(name = ["lsd.dist.connectionString"])
@ConditionalOnClass(Level::class)
open class FeignInterceptorConfig {

    @Bean
    @ConditionalOnMissingBean(Level::class)
    open fun feignLoggerLevel() = Level.BASIC

    @Bean
    open fun lsdFeignLoggerInterceptor(feignRequestCaptor: FeignRequestCaptor, feignResponseCaptor: FeignResponseCaptor) =
        LsdFeignLoggerInterceptor(feignRequestCaptor, feignResponseCaptor)

    @Bean
    open fun feignRequestCaptor(
        repositoryService: RepositoryService,
        sourceTargetDeriver: SourceTargetDeriver,
        traceIdRetriever: TraceIdRetriever,
        feignHttpHeaderRetriever: FeignHttpHeaderRetriever,
        @Value("\${spring.profiles.active:#{''}}") profile: String
    ) = FeignRequestCaptor(
        repositoryService, sourceTargetDeriver, traceIdRetriever, feignHttpHeaderRetriever, profile
    )

    @Bean
    open fun feignResponseCaptor(
        repositoryService: RepositoryService,
        sourceTargetDeriver: SourceTargetDeriver,
        traceIdRetriever: TraceIdRetriever,
        feignHttpHeaderRetriever: FeignHttpHeaderRetriever,
        @Value("\${spring.profiles.active:#{''}}") profile: String
    ) = FeignResponseCaptor(
        repositoryService, sourceTargetDeriver, traceIdRetriever, feignHttpHeaderRetriever, profile
    )

    @Bean
    open fun feignHttpHeaderRetriever(obfuscator: Obfuscator) = FeignHttpHeaderRetriever(obfuscator)
}
