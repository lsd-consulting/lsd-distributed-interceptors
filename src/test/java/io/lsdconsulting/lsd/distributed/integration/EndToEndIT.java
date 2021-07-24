package io.lsdconsulting.lsd.distributed.integration;

import com.lsd.LsdContext;
import io.lsdconsulting.junit5.LsdExtension;
import io.lsdconsulting.lsd.distributed.captor.repository.InterceptedDocumentRepository;
import io.lsdconsulting.lsd.distributed.captor.repository.model.InterceptedInteraction;
import io.lsdconsulting.lsd.distributed.integration.testapp.TestApplication;
import io.lsdconsulting.lsd.distributed.integration.testapp.config.RabbitConfig;
import io.lsdconsulting.lsd.distributed.integration.testapp.config.RabbitTemplateConfig;
import io.lsdconsulting.lsd.distributed.integration.testapp.config.RepositoryConfig;
import io.lsdconsulting.lsd.distributed.integration.testapp.config.RestConfig;
import io.lsdconsulting.lsd.distributed.integration.testapp.controller.event.SomethingDoneEvent;
import io.lsdconsulting.lsd.distributed.integration.testapp.repository.TestRepository;
import io.lsdconsulting.lsd.distributed.teststate.TestStateLogger;
import io.lsdconsulting.lsd.distributed.teststate.TraceIdGenerator;
import io.lsdconsulting.lsd.distributed.teststate.interaction.InteractionNameGenerator;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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

import javax.annotation.PostConstruct;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.lsd.ParticipantType.*;
import static io.lsdconsulting.lsd.distributed.captor.repository.model.Type.*;
import static io.lsdconsulting.lsd.distributed.integration.matcher.InterceptedInteractionMatcher.with;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.spy;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.RequestEntity.get;

@Slf4j
@SpringJUnitConfig(classes = {RepositoryConfig.class, RestConfig.class, RabbitConfig.class, RabbitTemplateConfig.class})
@SpringBootTest(webEnvironment = RANDOM_PORT, classes = {TestApplication.class})
@TestPropertySource("classpath:application-test.properties")
@AutoConfigureWireMock(port = 0)
@ExtendWith(LsdExtension.class)
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

    private TestStateLogger testStateLogger;

    @Autowired
    private InterceptedDocumentRepository interceptedDocumentRepository;

    @Autowired
    private InteractionNameGenerator interactionNameGenerator;

    private final String setupTraceId = TraceIdGenerator.generate();
    private final String mainTraceId = TraceIdGenerator.generate();

    private final LsdContext realContext = LsdContext.getInstance();
    private final LsdContext lsdContext = spy(realContext);
    private final ArgumentCaptor<String> argumentCaptor = ArgumentCaptor.forClass(String.class);

    @BeforeEach
    void setup() {
        testStateLogger = new TestStateLogger(interceptedDocumentRepository, interactionNameGenerator, lsdContext);
    }

    @PostConstruct
    void postConstruct() {
        lsdContext.addParticipants(List.of(
                ACTOR.called("Client"),
                PARTICIPANT.called("TestApp"),
                QUEUE.called("SomethingDoneEvent"),
                PARTICIPANT.called("UNKNOWN_TARGET"),
                PARTICIPANT.called("Downstream")
        ));
    }

    @AfterAll
    static void tearDown() {
        TestRepository.tearDownDatabase();
    }

    @Test
    void shouldRecordRestTemplateAndListenerInteractions() throws URISyntaxException {
        givenExternalApi();

        final ResponseEntity<String> response = sentRequest("/api-listener", mainTraceId, null, "TestApp");

        assertThat(response.getStatusCode(), is(HttpStatus.OK));
        assertThat(response.getBody(), is("response_from_controller"));

        final List<InterceptedInteraction> interceptedInteractions = new ArrayList<>();
        await().untilAsserted(() -> {
            final List<InterceptedInteraction> foundInterceptedInteractions = testRepository.findAll(mainTraceId);
            assertThat(foundInterceptedInteractions, hasSize(8));
            interceptedInteractions.addAll(foundInterceptedInteractions);
        });

        testStateLogger.captureInteractionsFromDatabase(mainTraceId);

        assertThat("REQUEST interaction missing", interceptedInteractions, hasItem(with(REQUEST, "TestApp", NO_BODY, "TestApp", "/api-listener?message=from_test")));
        assertThat("REQUEST interaction missing", interceptedInteractions, hasItem(with(RESPONSE, "TestApp", "response_from_controller", "TestApp", "/api-listener?message=from_test")));

        assertThat("PUBLISH interaction missing", interceptedInteractions, hasItem(with(PUBLISH, "TestApp", "{\"message\":\"from_controller\"}", "SomethingDoneEvent", "SomethingDoneEvent")));
        assertThat("CONSUMER interaction missing", interceptedInteractions, hasItem(with(CONSUME, "TestApp", "{\"message\":\"from_controller\"}", "SomethingDoneEvent", "SomethingDoneEvent")));

        assertThat("REQUEST interaction missing", interceptedInteractions, hasItem(with(REQUEST, "TestApp", "from_listener", "UNKNOWN_TARGET", "/external-api?message=from_feign")));
        assertThat("REQUEST interaction missing", interceptedInteractions, hasItem(with(RESPONSE, "TestApp", "from_external", "UNKNOWN_TARGET", "/external-api?message=from_feign")));
    }

    @Test
    void shouldRecordReceivingMessagesWithRabbitTemplate() throws URISyntaxException {
        final ResponseEntity<String> response = sentRequest("/api-rabbit-template", mainTraceId, "Client", "TestApp");

        assertThat(response.getStatusCode(), is(HttpStatus.OK));
        assertThat(response.getBody(), is("response_from_controller"));

        await().untilAsserted(() -> {
            final ParameterizedTypeReference<SomethingDoneEvent> type = new ParameterizedTypeReference<>() {
            };
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

        testStateLogger.captureInteractionsFromDatabase(mainTraceId);

        assertThat("REQUEST interaction missing", interceptedInteractions, hasItem(with(REQUEST, "Client", NO_BODY, "TestApp", "/api-rabbit-template?message=from_test")));
        assertThat("REQUEST interaction missing", interceptedInteractions, hasItem(with(RESPONSE, "Client", "response_from_controller", "TestApp", "/api-rabbit-template?message=from_test")));

        assertThat("PUBLISH interaction missing", interceptedInteractions, hasItem(with(PUBLISH, "TestApp", "{\"message\":\"from_controller\"}", "SomethingDoneEvent", "SomethingDoneEvent")));
        assertThat("CONSUMER interaction missing", interceptedInteractions, hasItem(with(CONSUME, "TestApp", "{\"message\":\"from_controller\"}", "SomethingDoneEvent", "SomethingDoneEvent")));
    }

    @Test
    void shouldRecordHeaderSuppliedNames() throws URISyntaxException {
        doNothing().when(lsdContext).capture(argumentCaptor.capture(), any());
        givenExternalApi();

        final ResponseEntity<String> response = sentRequest("/api-listener", mainTraceId, "Client", "TestApp");

        assertThat(response.getStatusCode(), is(HttpStatus.OK));
        assertThat(response.getBody(), containsString("response_from_controller"));

        await().untilAsserted(() -> assertThat(testRepository.findAll(mainTraceId), hasSize(8)));

        testStateLogger.captureInteractionsFromDatabase(mainTraceId);

        assertThat(argumentCaptor.getAllValues(), contains(
                "GET /api-listener?message=from_test from Client to TestApp",
                "publish event from TestApp to SomethingDoneEvent",
                "200 OK response from TestApp to Client",
                "consume message from SomethingDoneEvent to TestApp",
                "POST /external-api?message=from_feign from TestApp to UNKNOWN_TARGET",
                "200 OK response from UNKNOWN_TARGET to TestApp",
                "POST /external-api?message=from_feign from TestApp to Downstream",
                "200 OK response from Downstream to TestApp"));
    }

    @Test
    void shouldRecordHeaderSuppliedNamesWithDiagram() throws URISyntaxException {
        givenExternalApi();

        final ResponseEntity<String> response = sentRequest("/api-listener", mainTraceId, "Client", "TestApp");

        assertThat(response.getStatusCode(), is(HttpStatus.OK));
        assertThat(response.getBody(), containsString("response_from_controller"));

        await().untilAsserted(() -> assertThat(testRepository.findAll(mainTraceId), hasSize(8)));

      testStateLogger.captureInteractionsFromDatabase(mainTraceId);
    }

    @Test
    void shouldRecordHeaderSuppliedNamesWithColour() throws URISyntaxException {
        doNothing().when(lsdContext).capture(argumentCaptor.capture(), any());
        givenExternalApi();

        final ResponseEntity<String> response = sentRequest("/api-listener", mainTraceId, "Client", "TestApp");

        assertThat(response.getStatusCode(), is(HttpStatus.OK));
        assertThat(response.getBody(), containsString("response_from_controller"));

        await().untilAsserted(() -> assertThat(testRepository.findAll(mainTraceId), hasSize(8)));

        testStateLogger.captureInteractionsFromDatabase(Map.of(mainTraceId, Optional.of("[#blue]")));

        assertThat(argumentCaptor.getAllValues(), contains(
                "GET /api-listener?message=from_test from Client to TestApp [#blue]",
                "publish event from TestApp to SomethingDoneEvent [#blue]",
                "200 OK response from TestApp to Client [#blue]",
                "consume message from SomethingDoneEvent to TestApp [#blue]",
                "POST /external-api?message=from_feign from TestApp to UNKNOWN_TARGET [#blue]",
                "200 OK response from UNKNOWN_TARGET to TestApp [#blue]",
                "POST /external-api?message=from_feign from TestApp to Downstream [#blue]",
                "200 OK response from Downstream to TestApp [#blue]"));
    }

    @Test
    void shouldRecordHeaderSuppliedNamesWithMultipleTraceIds() throws URISyntaxException {
        doNothing().when(lsdContext).capture(argumentCaptor.capture(), any());

        givenExternalApi();

        sentRequest("/setup1", setupTraceId, "E2E", "Setup1");

        final ResponseEntity<String> response = sentRequest("/api-listener", mainTraceId, "Client", "TestApp");

        assertThat(response.getStatusCode(), is(HttpStatus.OK));
        assertThat(response.getBody(), containsString("response_from_controller"));

        await().untilAsserted(() -> assertThat(testRepository.findAll(mainTraceId), hasSize(8)));

        sentRequest("/setup2", setupTraceId, "E2E", "Setup2");

        testStateLogger.captureInteractionsFromDatabase(Map.of(mainTraceId, Optional.of("[#blue]"), setupTraceId, Optional.of("[#green]")));

        assertThat(argumentCaptor.getAllValues(), contains(
                "GET /setup1?message=from_test from E2E to Setup1 [#green]",
                "200 OK response from Setup1 to E2E [#green]",
                "GET /api-listener?message=from_test from Client to TestApp [#blue]",
                "publish event from TestApp to SomethingDoneEvent [#blue]",
                "200 OK response from TestApp to Client [#blue]",
                "consume message from SomethingDoneEvent to TestApp [#blue]",
                "POST /external-api?message=from_feign from TestApp to UNKNOWN_TARGET [#blue]",
                "200 OK response from UNKNOWN_TARGET to TestApp [#blue]",
                "POST /external-api?message=from_feign from TestApp to Downstream [#blue]",
                "200 OK response from Downstream to TestApp [#blue]",
                "GET /setup2?message=from_test from E2E to Setup2 [#green]",
                "200 OK response from Setup2 to E2E [#green]"));
    }

    @Test
    void shouldRecordHeaderSuppliedNamesWithMultipleTraceIdsWithDiagram() throws URISyntaxException {
        givenExternalApi();

        sentRequest("/setup1", setupTraceId, "E2E", "Setup1");

        final ResponseEntity<String> response = sentRequest("/api-listener", mainTraceId, "Client", "TestApp");

        assertThat(response.getStatusCode(), is(HttpStatus.OK));
        assertThat(response.getBody(), containsString("response_from_controller"));

        await().untilAsserted(() -> assertThat(testRepository.findAll(mainTraceId), hasSize(8)));

        sentRequest("/setup2", setupTraceId, "E2E", "Setup2");

        testStateLogger.captureInteractionsFromDatabase(Map.of(mainTraceId, Optional.of("[#blue]"), setupTraceId, Optional.of("[#green]")));
    }

    @Test
    void shouldRecordObfuscatedHeaders() throws URISyntaxException {
        givenExternalApi();

        final ResponseEntity<String> response = sentRequest("/api-listener", mainTraceId, null, "TestApp");

        assertThat(response.getStatusCode(), is(HttpStatus.OK));

        final List<InterceptedInteraction> interceptedInteractions = new ArrayList<>();
        await().untilAsserted(() -> {
            final List<InterceptedInteraction> foundInterceptedInteractions = testRepository.findAll(mainTraceId);
            assertThat(foundInterceptedInteractions, hasSize(8));
            interceptedInteractions.addAll(foundInterceptedInteractions);
        });

        assertThat("Header obfuscation did not work ", interceptedInteractions, not(hasItem(hasProperty("requestHeaders", hasEntry("Authorization", List.of("Password"))))));
    }

    private void givenExternalApi() {
        stubFor(post(urlEqualTo("/external-api?message=from_feign"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBody("from_external")));
    }

    private ResponseEntity<String> sentRequest(final String resourceName, final String traceId, final String sourceName, final String targetName) throws URISyntaxException {
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
