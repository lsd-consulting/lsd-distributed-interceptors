package io.lsdconsulting.lsd.distributed.interceptor.integration.testapp.external;

import feign.RequestInterceptor;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.PostMapping;

import static io.lsdconsulting.lsd.distributed.interceptor.integration.testapp.external.ExternalClient.ClientConfig;

@FeignClient(name = "externalClient", url = "http://localhost:${wiremock.server.port}", configuration = ClientConfig.class)
public interface ExternalClient {

    @PostMapping("/external-api?message=from_feign")
    void post(String message);

    class ClientConfig {
        @Bean
        public RequestInterceptor headersInterceptor() {
            return template -> template
                    .header("Authorization", "Password");
        }
    }
}
