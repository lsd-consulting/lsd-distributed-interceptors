package io.lsdconsulting.lsd.distributed.interceptor.integration;

import io.lsdconsulting.lsd.distributed.interceptor.integration.testapp.TestApplication;
import io.lsdconsulting.lsd.distributed.interceptor.integration.testapp.config.RabbitConfig;
import io.lsdconsulting.lsd.distributed.interceptor.integration.testapp.config.RabbitTemplateConfig;
import io.lsdconsulting.lsd.distributed.interceptor.integration.testapp.config.RepositoryConfig;
import io.lsdconsulting.lsd.distributed.interceptor.integration.testapp.config.RestConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.context.annotation.Import;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;

import java.net.URI;
import java.net.URISyntaxException;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.RequestEntity.get;

@Slf4j
@Import({RepositoryConfig.class, RestConfig.class, RabbitConfig.class, RabbitTemplateConfig.class})
@SpringBootTest(webEnvironment = RANDOM_PORT, classes = {TestApplication.class})
@TestPropertySource("classpath:application-test.properties")
@AutoConfigureWireMock(port = 0)
public class IntegrationTestBase {

    @LocalServerPort
    private int serverPort;

    @Autowired
    private TestRestTemplate testRestTemplate;

    void givenExternalApi() {
        stubFor(post(urlEqualTo("/external-api?message=from_feign"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBody("from_external")));
    }

    ResponseEntity<String> sentRequest(final String resourceName, final String traceId, final String sourceName, final String targetName) throws URISyntaxException {
        log.info("Sending traceId:{}", traceId);
        final RequestEntity<?> requestEntity = get(new URI("http://localhost:" + serverPort + resourceName + "?message=from_test"))
                .header("Content-Type", APPLICATION_JSON_VALUE)
                .header("b3", traceId + "-" + traceId + "-1")
                .header("Source-Name", sourceName)
                .header("Target-Name", targetName)
                .header("Authorization", "Basic password")
                .build();

        return testRestTemplate.exchange(requestEntity, String.class);
    }
}
