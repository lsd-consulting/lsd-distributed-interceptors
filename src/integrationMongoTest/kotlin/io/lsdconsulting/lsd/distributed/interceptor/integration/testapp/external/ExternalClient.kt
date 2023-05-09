package io.lsdconsulting.lsd.distributed.interceptor.integration.testapp.external

import feign.RequestInterceptor
import feign.RequestTemplate
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.context.annotation.Bean
import org.springframework.web.bind.annotation.PostMapping

@FeignClient(
    name = "externalClient",
    url = "http://localhost:\${wiremock.server.port}",
    configuration = [ExternalClient.ClientConfig::class]
)
interface ExternalClient {
    @PostMapping("/external-api?message=from_feign")
    fun post(message: String?)

    class ClientConfig {
        @Bean
        fun headersInterceptor(): RequestInterceptor {
            return RequestInterceptor { template: RequestTemplate ->
                template
                    .header("Authorization", "Password")
            }
        }
    }
}
