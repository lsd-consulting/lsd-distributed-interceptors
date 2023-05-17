package io.lsdconsulting.lsd.distributed.interceptor.integration.testapp.external

import feign.RequestInterceptor
import feign.RequestTemplate
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.context.annotation.Bean
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam

@FeignClient(
    name = "externalClient",
    url = "http://localhost:\${server.port}",
    configuration = [ExternalClient.ClientConfig::class]
)
interface ExternalClient {
    @GetMapping("/get-api")
    fun get(@RequestParam message: String): String

    class ClientConfig {
        @Bean
        fun headersInterceptor(): RequestInterceptor {
            return RequestInterceptor { template: RequestTemplate ->
                template
                    .header("Authorization", "Password")
                    .header("b3", "3e316fc2da26a3c7-3e316fc2da26a3c7-1")
            }
        }
    }
}
