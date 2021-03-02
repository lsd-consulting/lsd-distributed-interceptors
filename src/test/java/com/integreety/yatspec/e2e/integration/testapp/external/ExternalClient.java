package com.integreety.yatspec.e2e.integration.testapp.external;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient(name = "externalClient", url = "http://localhost:${wiremock.server.port}")
public interface ExternalClient {

    @PostMapping("/external-api?message=from_feign")
    void post(String message);
}
