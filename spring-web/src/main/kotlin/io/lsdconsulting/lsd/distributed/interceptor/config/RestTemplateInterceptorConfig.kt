package io.lsdconsulting.lsd.distributed.interceptor.config

import io.lsdconsulting.lsd.distributed.interceptor.captor.http.RequestCaptor
import io.lsdconsulting.lsd.distributed.interceptor.captor.http.ResponseCaptor
import io.lsdconsulting.lsd.distributed.interceptor.interceptor.LsdRestTemplateCustomizer
import io.lsdconsulting.lsd.distributed.interceptor.interceptor.LsdRestTemplateInterceptor
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.client.ClientHttpRequestInterceptor
import org.springframework.web.client.RestTemplate

@Configuration
@ConditionalOnProperty(name = ["lsd.dist.connectionString"])
@ConditionalOnClass(RestTemplate::class)
open class RestTemplateInterceptorConfig {
    @Bean
    open fun lsdRestTemplateInterceptor(
        requestCaptor: RequestCaptor,
        responseCaptor: ResponseCaptor
    ): ClientHttpRequestInterceptor {
        return LsdRestTemplateInterceptor(requestCaptor, responseCaptor)
    }

    @Bean
    open fun lsdRestTemplateCustomizer(lsdRestTemplateInterceptor: ClientHttpRequestInterceptor?): LsdRestTemplateCustomizer {
        return LsdRestTemplateCustomizer(lsdRestTemplateInterceptor!!)
    }
}
