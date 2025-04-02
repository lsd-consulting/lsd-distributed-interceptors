package io.lsdconsulting.lsd.distributed.interceptor.config

import feign.Logger.Level
import io.lsdconsulting.lsd.distributed.interceptor.captor.FeignHttpHeaderRetriever
import io.lsdconsulting.lsd.distributed.interceptor.captor.FeignRequestCaptor
import io.lsdconsulting.lsd.distributed.interceptor.captor.FeignResponseCaptor
import io.lsdconsulting.lsd.distributed.interceptor.captor.common.Obfuscator
import io.lsdconsulting.lsd.distributed.interceptor.captor.http.SourceTargetDeriver
import io.lsdconsulting.lsd.distributed.interceptor.captor.trace.TraceIdRetriever
import io.lsdconsulting.lsd.distributed.interceptor.interceptor.LsdFeignLoggerInterceptor
import io.lsdconsulting.lsd.distributed.interceptor.persistence.RepositoryService
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
@ConditionalOnProperty(name = ["lsd.dist.connectionString"])
@ConditionalOnClass(Level::class)
class FeignInterceptorConfig {

    @Bean
    @ConditionalOnMissingBean(Level::class)
    fun feignLoggerLevel() = Level.BASIC

    @Bean
    fun lsdFeignLoggerInterceptor(feignRequestCaptor: FeignRequestCaptor, feignResponseCaptor: FeignResponseCaptor) =
        LsdFeignLoggerInterceptor(feignRequestCaptor, feignResponseCaptor)

    @Bean
    fun feignRequestCaptor(
        repositoryService: RepositoryService,
        sourceTargetDeriver: SourceTargetDeriver,
        traceIdRetriever: TraceIdRetriever,
        feignHttpHeaderRetriever: FeignHttpHeaderRetriever,
        @Value("\${spring.profiles.active:#{''}}") profile: String
    ) = FeignRequestCaptor(
        repositoryService, sourceTargetDeriver, traceIdRetriever, feignHttpHeaderRetriever, profile
    )

    @Bean
    fun feignResponseCaptor(
        repositoryService: RepositoryService,
        sourceTargetDeriver: SourceTargetDeriver,
        traceIdRetriever: TraceIdRetriever,
        feignHttpHeaderRetriever: FeignHttpHeaderRetriever,
        @Value("\${spring.profiles.active:#{''}}") profile: String
    ) = FeignResponseCaptor(
        repositoryService, sourceTargetDeriver, traceIdRetriever, feignHttpHeaderRetriever, profile
    )

    @Bean
    fun feignHttpHeaderRetriever(obfuscator: Obfuscator) = FeignHttpHeaderRetriever(obfuscator)
}
