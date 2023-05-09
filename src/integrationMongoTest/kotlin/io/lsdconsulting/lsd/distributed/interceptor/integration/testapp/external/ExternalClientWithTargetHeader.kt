package io.lsdconsulting.lsd.distributed.interceptor.integration.testapp.external

import feign.RequestInterceptor
import feign.RequestTemplate
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.context.annotation.Bean
import org.springframework.web.bind.annotation.PostMapping

@FeignClient(
    name = "externalClientTargetHeader",
    url = "http://localhost:\${server.wiremock.port}",
    configuration = [ExternalClientWithTargetHeader.ClientConfig::class]
)
interface ExternalClientWithTargetHeader {
    @PostMapping("/external-api?message=from_feign")
    fun post(message: String?)

    class ClientConfig {
        @Bean
        fun headersInterceptor(): RequestInterceptor {
            return RequestInterceptor { template: RequestTemplate ->
                template
                    .header("Authorization", "Password")
                    .header("Target-Name", "Downstream")
            }
        }
    }
}
