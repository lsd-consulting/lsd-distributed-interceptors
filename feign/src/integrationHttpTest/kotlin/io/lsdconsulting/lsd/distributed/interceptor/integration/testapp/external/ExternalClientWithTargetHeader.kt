package io.lsdconsulting.lsd.distributed.interceptor.integration.testapp.external

import feign.RequestInterceptor
import feign.RequestTemplate
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.context.annotation.Bean
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam

@FeignClient(
    name = "externalClientTargetHeader",
    url = "http://localhost:\${server.port}",
    configuration = [ExternalClientWithTargetHeader.ClientConfig::class]
)
interface ExternalClientWithTargetHeader {
    @PostMapping("/post-api")
    fun post(@RequestBody message: String): String

    class ClientConfig {
        @Bean
        fun headersInterceptor(): RequestInterceptor {
            return RequestInterceptor { template: RequestTemplate ->
                template
                    .header("b3", "3e316fc2da26a3c7-3e316fc2da26a3c7-1")
                    .header("Source-Name", "Upstream")
                    .header("Target-Name", "Downstream")
            }
        }
    }
}
