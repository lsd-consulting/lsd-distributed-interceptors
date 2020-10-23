package com.integreety.yatspec.e2e.integration;

import com.googlecode.yatspec.state.givenwhenthen.TestState;
import com.integreety.yatspec.e2e.captor.repository.model.InterceptedInteraction;
import com.integreety.yatspec.e2e.integration.testapp.TestApplication;
import com.integreety.yatspec.e2e.integration.testapp.config.RabbitConfig;
import com.integreety.yatspec.e2e.integration.testapp.config.RabbitTemplateConfig;
import com.integreety.yatspec.e2e.integration.testapp.config.RepositoryConfig;
import com.integreety.yatspec.e2e.integration.testapp.config.RestConfig;
import com.integreety.yatspec.e2e.integration.testapp.repository.TestRepository;
import com.integreety.yatspec.e2e.teststate.TestStateLogger;
import com.integreety.yatspec.e2e.teststate.mapper.destination.DestinationNameMappings;
import com.integreety.yatspec.e2e.teststate.mapper.source.SourceNameMappings;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
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
import java.util.Map;
import java.util.Set;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.integreety.yatspec.e2e.captor.repository.model.Type.*;
import static com.integreety.yatspec.e2e.integration.matcher.InterceptedInteractionMatcher.with;
import static com.integreety.yatspec.e2e.teststate.TraceIdGenerator.generate;
import static com.integreety.yatspec.e2e.teststate.mapper.destination.UserSuppliedDestinationMappings.userSuppliedDestinationMappings;
import static com.integreety.yatspec.e2e.teststate.mapper.source.UserSuppliedSourceMappings.userSuppliedSourceMappings;
import static org.apache.commons.lang3.tuple.Pair.of;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.RequestEntity.get;

@Slf4j
@SpringJUnitConfig(classes = {RepositoryConfig.class, RestConfig.class, RabbitConfig.class, RabbitTemplateConfig.class})
@SpringBootTest(webEnvironment = RANDOM_PORT, classes = {TestApplication.class})
@TestPropertySource("classpath:application-test.properties")
@AutoConfigureWireMock(port = 0)
public class EndToEndIT {

    private static final String NO_BODY = "";

    @LocalServerPort
    private int serverPort;

    @Autowired
    private TestRepository testRepository;

    @Autowired
    private TestRestTemplate testRestTemplate;

    @Autowired
    private TestStateLogger testStateLogger;

    @Autowired
    private TestState testState;

    private final String traceId = generate();

    @AfterAll
    public static void tearDown() {
        TestRepository.tearDownDatabase();
    }

    @Test
    public void shouldRecordAllInteractions() throws URISyntaxException {

        givenExternalApi();

        final ResponseEntity<String> response = sendInitialRequest(traceId);

        assertThat(response.getStatusCode(), is(HttpStatus.OK));
        assertThat(response.getBody(), is("response_from_controller"));

        final List<InterceptedInteraction> interceptedInteractions = new ArrayList<>();
        await().untilAsserted(() -> {
            final List<InterceptedInteraction> foundInterceptedInteractions = testRepository.findAll(traceId);
            assertThat(foundInterceptedInteractions, hasSize(6));
            interceptedInteractions.addAll(foundInterceptedInteractions);
        });

        assertThat("REQUEST interaction missing", interceptedInteractions, hasItem(with(REQUEST, "lsdEnd2End", NO_BODY, "/objects?message=from_test"))); // TODO Need to assert the parameter value
        assertThat("REQUEST interaction missing", interceptedInteractions, hasItem(with(RESPONSE, "lsdEnd2End", "response_from_controller", "/objects?message=from_test")));

        // TODO Uncomment once the exchange name determination mechanism is fixed
        // assertThat("PUBLISH interaction missing", interceptedInteractions, hasItem(with(PUBLISH, "lsdEnd2End", "from_controller", "exchange")));
        assertThat("CONSUMER interaction missing", interceptedInteractions, hasItem(with(CONSUME, "lsdEnd2End", "from_controller", "exchange")));

        assertThat("REQUEST interaction missing", interceptedInteractions, hasItem(with(REQUEST, "lsdEnd2End", "from_listener", "/external-objects?message=from_feign")));
        assertThat("REQUEST interaction missing", interceptedInteractions, hasItem(with(RESPONSE, "lsdEnd2End", "from_external", "/external-objects?message=from_feign")));
    }

    @Test
    public void shouldRecordUserSuppliedNames() throws URISyntaxException {
        givenExternalApi();

        final ResponseEntity<String> response = sendInitialRequest(traceId);

        assertThat(response.getStatusCode(), is(HttpStatus.OK));
        assertThat(response.getBody(), containsString("response_from_controller"));

        await().untilAsserted(() -> assertThat(testRepository.findAll(traceId), hasSize(6)));

        testStateLogger.logStatesFromDatabase(traceId, sourceNameMappings, destinationNameMappings);

        final Set<String> interactionNames = testState.getCapturedTypes().keySet();
        assertThat(interactionNames, hasItem("GET /objects?message=from_test from Client to Controller"));
        assertThat(interactionNames, hasItem("publish event from Controller to Exchange"));
        assertThat(interactionNames, hasItem("200 OK response from Controller to Client"));
        assertThat(interactionNames, hasItem("consume message from Exchange to Consumer"));
        assertThat(interactionNames, hasItem("POST /external-objects?message=from_feign from Consumer to Wiremock"));
        assertThat(interactionNames, hasItem("200 OK response from Wiremock to Consumer"));
    }

    private final SourceNameMappings sourceNameMappings = userSuppliedSourceMappings(Map.of(
            of("lsdEnd2End", "/objects?message=from_test"), "Client",
            of("lsdEnd2End", ""), "Controller",
            of("lsdEnd2End", "exchange"), "Consumer",
            of("lsdEnd2End", "/external-objects?message=from_feign"), "Consumer"
    ));

    private final DestinationNameMappings destinationNameMappings = userSuppliedDestinationMappings(Map.of(
            "/objects?message=from_test", "Controller",
            "", "Exchange",
            "exchange", "Exchange",
            "/external-objects?message=from_feign", "Wiremock"
    ));

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
