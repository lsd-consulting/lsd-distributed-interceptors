package com.integreety.yatspec.e2e.integration;

import com.integreety.yatspec.e2e.captor.repository.model.InterceptedCall;
import com.integreety.yatspec.e2e.integration.testapp.TestApplication;
import com.integreety.yatspec.e2e.integration.testapp.config.EndToEndConfiguration;
import com.integreety.yatspec.e2e.integration.testapp.repository.TestRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.http.HttpStatus;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.integreety.yatspec.e2e.captor.repository.model.Type.*;
import static com.integreety.yatspec.e2e.integration.matcher.InterceptedCallMatcher.with;
import static com.integreety.yatspec.e2e.teststate.TraceIdGenerator.generate;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.RequestEntity.get;

@Slf4j
@SpringBootTest(webEnvironment = RANDOM_PORT, classes = {TestApplication.class})
@SpringJUnitConfig(classes = EndToEndConfiguration.class)
@TestPropertySource("classpath:application-test.properties")
@Execution(ExecutionMode.SAME_THREAD)
@AutoConfigureWireMock(port = 0)
public class EndToEndIT {

    private static final String NO_BODY = "";

    @LocalServerPort
    private int serverPort;

    @Autowired
    private TestRestTemplate testRestTemplate;

    private final String traceId = generate();
    private final TestRepository testRepository = new TestRepository();

    @Test
    public void shouldRecordAllInteractions() throws URISyntaxException {

        givenExternalApi();

        final ResponseEntity<String> response = sendInitialRequest(traceId);

        assertThat(response.getStatusCode(), is(HttpStatus.OK));
        assertThat(response.getBody(), is("response_from_controller"));

        final List<InterceptedCall> interceptedCalls = new ArrayList<>();
        await().untilAsserted(() -> {
            final List<InterceptedCall> foundInterceptedCalls = testRepository.findAll(traceId);
            assertThat(foundInterceptedCalls, hasSize(6));
            interceptedCalls.addAll(foundInterceptedCalls);
        });

        assertThat("REQUEST interaction missing", interceptedCalls, hasItem(with(REQUEST, "lsdEnd2End", NO_BODY, "/objects?message=from_test"))); // TODO Need to assert the parameter value
        assertThat("REQUEST interaction missing", interceptedCalls, hasItem(with(RESPONSE, "lsdEnd2End", "response_from_controller", "/objects?message=from_test")));

        // TODO Uncomment once the exchange name determination mechanism is fixed
        // assertThat("PUBLISH interaction missing", interceptedCalls, hasItem(with(PUBLISH, "lsdEnd2End", "from_controller", "exchange")));
        assertThat("CONSUMER interaction missing", interceptedCalls, hasItem(with(CONSUME, "lsdEnd2End", "from_controller", "exchange")));

        assertThat("REQUEST interaction missing", interceptedCalls, hasItem(with(REQUEST, "lsdEnd2End", "from_listener", "/external-objects?message=from_feign")));
        assertThat("REQUEST interaction missing", interceptedCalls, hasItem(with(RESPONSE, "lsdEnd2End", "from_external", "/external-objects?message=from_feign")));
    }

    private void givenExternalApi() {
        stubFor(post(urlEqualTo("/external-objects?message=from_feign"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBody("from_external")));
    }

    public ResponseEntity<String> sendInitialRequest(final String traceId) throws URISyntaxException {
        log.info("Sending traceId:{}", traceId);
        final RequestEntity<?> requestEntity = get(new URI("http://localhost:" + serverPort + "/objects?message=from_test"))
                .header("Content-Type", APPLICATION_JSON_VALUE)
                .header("b3", traceId + "-" + traceId + "-1")
                .build();

        return testRestTemplate.exchange(requestEntity, String.class);
    }
}
