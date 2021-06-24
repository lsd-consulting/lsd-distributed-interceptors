package com.integreety.yatspec.e2e.integration;

import com.integreety.yatspec.e2e.captor.repository.model.InterceptedInteraction;
import com.integreety.yatspec.e2e.integration.testapp.TestApplication;
import com.integreety.yatspec.e2e.integration.testapp.config.RabbitConfig;
import com.integreety.yatspec.e2e.integration.testapp.config.RabbitTemplateConfig;
import com.integreety.yatspec.e2e.integration.testapp.config.RepositoryConfig;
import com.integreety.yatspec.e2e.integration.testapp.config.RestConfig;
import com.integreety.yatspec.e2e.integration.testapp.controller.event.SomethingDoneEvent;
import com.integreety.yatspec.e2e.integration.testapp.repository.TestRepository;
import com.integreety.yatspec.e2e.teststate.TestStateLogger;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.core.ParameterizedTypeReference;
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
import java.util.Optional;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.integreety.yatspec.e2e.captor.repository.model.Type.*;
import static com.integreety.yatspec.e2e.integration.matcher.InterceptedInteractionMatcher.with;
import static com.integreety.yatspec.e2e.teststate.TraceIdGenerator.generate;
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
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private TestStateLogger testStateLogger;

    private final String setupTraceId = generate();
    private final String mainTraceId = generate();

    @AfterAll
    public static void tearDown() {
        TestRepository.tearDownDatabase();
    }

    @Test
    public void shouldRecordRestTemplateAndListenerInteractions() throws URISyntaxException {

        givenExternalApi();

        final ResponseEntity<String> response = sentRequest("/api-listener", mainTraceId, null, "Controller");

        assertThat(response.getStatusCode(), is(HttpStatus.OK));
        assertThat(response.getBody(), is("response_from_controller"));

        final List<InterceptedInteraction> interceptedInteractions = new ArrayList<>();
        await().untilAsserted(() -> {
            final List<InterceptedInteraction> foundInterceptedInteractions = testRepository.findAll(mainTraceId);
            assertThat(foundInterceptedInteractions, hasSize(8));
            interceptedInteractions.addAll(foundInterceptedInteractions);
        });

        testStateLogger.logStatesFromDatabase(mainTraceId);

        assertThat("REQUEST interaction missing", interceptedInteractions, hasItem(with(REQUEST, "lsdEnd2End", NO_BODY, "Controller", "/api-listener?message=from_test"))); // TODO Need to assert the parameter value
        assertThat("REQUEST interaction missing", interceptedInteractions, hasItem(with(RESPONSE, "lsdEnd2End", "response_from_controller", "Controller", "/api-listener?message=from_test")));

        assertThat("PUBLISH interaction missing", interceptedInteractions, hasItem(with(PUBLISH, "lsdEnd2End", "{\"message\":\"from_controller\"}", "SomethingDoneEvent", "SomethingDoneEvent")));
        assertThat("CONSUMER interaction missing", interceptedInteractions, hasItem(with(CONSUME, "lsdEnd2End", "{\"message\":\"from_controller\"}", "SomethingDoneEvent", "SomethingDoneEvent")));

        assertThat("REQUEST interaction missing", interceptedInteractions, hasItem(with(REQUEST, "lsdEnd2End", "from_listener", "UNKNOWN_TARGET", "/external-api?message=from_feign")));
        assertThat("REQUEST interaction missing", interceptedInteractions, hasItem(with(RESPONSE, "lsdEnd2End", "from_external", "UNKNOWN_TARGET", "/external-api?message=from_feign")));
    }

    @Test
    public void shouldRecordReceivingMessagesWithRabbitTemplate() throws URISyntaxException {

        final ResponseEntity<String> response = sentRequest("/api-rabbit-template", mainTraceId, "Client", "Controller");

        assertThat(response.getStatusCode(), is(HttpStatus.OK));
        assertThat(response.getBody(), is("response_from_controller"));

        await().untilAsserted(() -> {
            final ParameterizedTypeReference<SomethingDoneEvent> type = new ParameterizedTypeReference<>() {};
            final SomethingDoneEvent message = rabbitTemplate.receiveAndConvert("queue-rabbit-template", 2000, type);
            assertThat(message, is(notNullValue()));
            assertThat(message.getMessage(), is("from_controller"));
        });

        final List<InterceptedInteraction> interceptedInteractions = new ArrayList<>();
        await().untilAsserted(() -> {
            final List<InterceptedInteraction> foundInterceptedInteractions = testRepository.findAll(mainTraceId);
            assertThat(foundInterceptedInteractions, hasSize(4));
            interceptedInteractions.addAll(foundInterceptedInteractions);
        });

        testStateLogger.logStatesFromDatabase(mainTraceId);

        assertThat("REQUEST interaction missing", interceptedInteractions, hasItem(with(REQUEST, "Client", NO_BODY, "Controller", "/api-rabbit-template?message=from_test"))); // TODO Need to assert the parameter value
        assertThat("REQUEST interaction missing", interceptedInteractions, hasItem(with(RESPONSE, "Client", "response_from_controller", "Controller", "/api-rabbit-template?message=from_test")));

        assertThat("PUBLISH interaction missing", interceptedInteractions, hasItem(with(PUBLISH, "lsdEnd2End", "{\"message\":\"from_controller\"}", "SomethingDoneEvent", "SomethingDoneEvent")));
        assertThat("CONSUMER interaction missing", interceptedInteractions, hasItem(with(CONSUME, "lsdEnd2End", "{\"message\":\"from_controller\"}", "SomethingDoneEvent", "SomethingDoneEvent")));
    }

    @Test
    public void shouldRecordHeaderSuppliedNames() throws URISyntaxException {
        givenExternalApi();

        final ResponseEntity<String> response = sentRequest("/api-listener", mainTraceId, "Client", "Controller");

        assertThat(response.getStatusCode(), is(HttpStatus.OK));
        assertThat(response.getBody(), containsString("response_from_controller"));

        await().untilAsserted(() -> assertThat(testRepository.findAll(mainTraceId), hasSize(8)));

        testStateLogger.logStatesFromDatabase(mainTraceId);

//        final Set<String> interactionNames = testState.getCapturedTypes().keySet();
//        assertThat(interactionNames, contains("GET /api-listener?message=from_test from Client to Controller",
//        "publish event from lsdEnd2End to SomethingDoneEvent",
//        "200 OK response from Controller to Client",
//        "consume message from SomethingDoneEvent to lsdEnd2End",
//        "POST /external-api?message=from_feign from lsdEnd2End to UNKNOWN_TARGET",
//        "200 OK response from UNKNOWN_TARGET to lsdEnd2End",
//        "POST /external-api?message=from_feign from lsdEnd2End to Downstream",
//        "200 OK response from Downstream to lsdEnd2End"));
    }

    @Test
    public void shouldRecordHeaderSuppliedNamesWithColour() throws URISyntaxException {
        givenExternalApi();

        final ResponseEntity<String> response = sentRequest("/api-listener", mainTraceId, "Client", "Controller");

        assertThat(response.getStatusCode(), is(HttpStatus.OK));
        assertThat(response.getBody(), containsString("response_from_controller"));

        await().untilAsserted(() -> assertThat(testRepository.findAll(mainTraceId), hasSize(8)));

        testStateLogger.logStatesFromDatabase(Map.of(mainTraceId, Optional.of("[#colour1]")));

//        final Set<String> interactionNames = testState.getCapturedTypes().keySet();
//        assertThat(interactionNames, contains("GET /api-listener?message=from_test from Client to Controller [#colour1]",
//                "publish event from lsdEnd2End to SomethingDoneEvent [#colour1]",
//                "200 OK response from Controller to Client [#colour1]",
//                "consume message from SomethingDoneEvent to lsdEnd2End [#colour1]",
//                "POST /external-api?message=from_feign from lsdEnd2End to UNKNOWN_TARGET [#colour1]",
//                "200 OK response from UNKNOWN_TARGET to lsdEnd2End [#colour1]",
//                "POST /external-api?message=from_feign from lsdEnd2End to Downstream [#colour1]",
//                "200 OK response from Downstream to lsdEnd2End [#colour1]"));
    }

    @Test
    public void shouldRecordHeaderSuppliedNamesWithMultipleTraceIds() throws URISyntaxException {
        givenExternalApi();

        sentRequest("/setup1", setupTraceId, "E2E", "Setup1");

        final ResponseEntity<String> response = sentRequest("/api-listener", mainTraceId, "Client", "Controller");

        assertThat(response.getStatusCode(), is(HttpStatus.OK));
        assertThat(response.getBody(), containsString("response_from_controller"));

        await().untilAsserted(() -> assertThat(testRepository.findAll(mainTraceId), hasSize(8)));

        sentRequest("/setup2", setupTraceId, "E2E", "Setup2");

        testStateLogger.logStatesFromDatabase(Map.of(mainTraceId, Optional.of("[#colour1]"), setupTraceId, Optional.of("[#colour2]")));

//        final Set<String> interactionNames = testState.getCapturedTypes().keySet();
//        assertThat(interactionNames, contains(
//                "GET /setup1?message=from_test from E2E to Setup1 [#colour2]",
//                "200 OK response from Setup1 to E2E [#colour2]",
//                "GET /api-listener?message=from_test from Client to Controller [#colour1]",
//                "publish event from lsdEnd2End to SomethingDoneEvent [#colour1]",
//                "200 OK response from Controller to Client [#colour1]",
//                "consume message from SomethingDoneEvent to lsdEnd2End [#colour1]",
//                "POST /external-api?message=from_feign from lsdEnd2End to UNKNOWN_TARGET [#colour1]",
//                "200 OK response from UNKNOWN_TARGET to lsdEnd2End [#colour1]",
//                "POST /external-api?message=from_feign from lsdEnd2End to Downstream [#colour1]",
//                "200 OK response from Downstream to lsdEnd2End [#colour1]",
//                "GET /setup2?message=from_test from E2E to Setup2 [#colour2]",
//                "200 OK response from Setup2 to E2E [#colour2]"));
    }

    private void givenExternalApi() {
        stubFor(post(urlEqualTo("/external-api?message=from_feign"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBody("from_external")));
    }

    public ResponseEntity<String> sentRequest(final String resourceName, final String traceId, final String sourceName, final String targetName) throws URISyntaxException {
        log.info("Sending traceId:{}", traceId);
        final RequestEntity<?> requestEntity = get(new URI("http://localhost:" + serverPort + resourceName + "?message=from_test"))
                .header("Content-Type", APPLICATION_JSON_VALUE)
                .header("b3", traceId + "-" + traceId + "-1")
                .header("Source-Name", sourceName)
                .header("Target-Name", targetName)
                .build();

        return testRestTemplate.exchange(requestEntity, String.class);
    }
}
