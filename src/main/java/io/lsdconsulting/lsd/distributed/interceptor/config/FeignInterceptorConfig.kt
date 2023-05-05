package io.lsdconsulting.lsd.distributed.interceptor.config

import feign.Logger.Level
import io.lsdconsulting.lsd.distributed.interceptor.captor.http.RequestCaptor
import io.lsdconsulting.lsd.distributed.interceptor.captor.http.ResponseCaptor
import io.lsdconsulting.lsd.distributed.interceptor.interceptor.LsdFeignLoggerInterceptor
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.cloud.openfeign.FeignClientBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
@ConditionalOnProperty(name = ["lsd.dist.connectionString"])
@ConditionalOnClass(FeignClientBuilder::class, Level::class)
open class FeignInterceptorConfig {
    @Bean
    @ConditionalOnMissingBean(Level::class)
    open fun feignLoggerLevel() = Level.BASIC

    @Bean
    open fun lsdFeignLoggerInterceptor(
        requestCaptor: RequestCaptor,
        responseCaptor: ResponseCaptor
    ) = LsdFeignLoggerInterceptor(requestCaptor, responseCaptor)
}
