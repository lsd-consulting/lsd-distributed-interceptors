package com.integreety.yatspec.e2e.integration.testapp.external;

import feign.RequestInterceptor;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.PostMapping;

import static com.integreety.yatspec.e2e.integration.testapp.external.ExternalClientWithTargetHeader.ClientConfig;

@FeignClient(name = "externalClientTargetHeader", url = "http://localhost:${wiremock.server.port}", configuration = ClientConfig.class)
public interface ExternalClientWithTargetHeader {

    @PostMapping("/external-api?message=from_feign")
    void post(String message);

    class ClientConfig {
        @Bean
        public RequestInterceptor headersInterceptor() {
            return template -> template.header("Target-Name", "Downstream");
        }
    }
}
