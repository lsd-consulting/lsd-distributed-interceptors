package io.lsdconsulting.lsd.distributed.integration;

import com.lsd.LsdContext;
import com.lsd.OutcomeStatus;
import io.lsdconsulting.lsd.distributed.captor.repository.InterceptedDocumentRepository;
import io.lsdconsulting.lsd.distributed.diagram.LsdLogger;
import io.lsdconsulting.lsd.distributed.diagram.TraceIdGenerator;
import io.lsdconsulting.lsd.distributed.diagram.interaction.InteractionGenerator;
import io.lsdconsulting.lsd.distributed.integration.testapp.TestApplication;
import io.lsdconsulting.lsd.distributed.integration.testapp.config.RabbitConfig;
import io.lsdconsulting.lsd.distributed.integration.testapp.config.RabbitTemplateConfig;
import io.lsdconsulting.lsd.distributed.integration.testapp.config.RepositoryConfig;
import io.lsdconsulting.lsd.distributed.integration.testapp.config.RestConfig;
import io.lsdconsulting.lsd.distributed.integration.testapp.repository.TestRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.lsd.ParticipantType.*;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@Slf4j
@SpringJUnitConfig(classes = {RepositoryConfig.class, RestConfig.class, RabbitConfig.class, RabbitTemplateConfig.class})
@SpringBootTest(webEnvironment = RANDOM_PORT, classes = {TestApplication.class})
@TestPropertySource("classpath:application-test.properties")
@AutoConfigureWireMock(port = 0)
// This is the only integration test that generates LSDs
public class EndToEndIT extends IntegrationTestBase {

    @Autowired
    private TestRepository testRepository;

    @Autowired
    private InterceptedDocumentRepository interceptedDocumentRepository;

    @Autowired
    private InteractionGenerator interactionGenerator;

    private final LsdContext lsdContext = new LsdContext();
    private final String mainTraceId = TraceIdGenerator.generate();
    private final String setupTraceId1 = TraceIdGenerator.generate();
    private final String setupTraceId2 = TraceIdGenerator.generate();

    private LsdLogger lsdLogger;

    @BeforeEach
    void setup() {
        lsdLogger = new LsdLogger(interceptedDocumentRepository, interactionGenerator, lsdContext);
    }

    @Test
    void shouldGenerateLsdWithSuppliedNames() throws URISyntaxException {
        lsdContext.addParticipants(List.of(
                ACTOR.called("Client"),
                PARTICIPANT.called("TestApp"),
                QUEUE.called("SomethingDoneEvent"),
                PARTICIPANT.called("UNKNOWN_TARGET"),
                PARTICIPANT.called("Downstream")
        ));
        givenExternalApi();

        final ResponseEntity<String> response = sentRequest("/api-listener", mainTraceId, "Client", "TestApp");

        assertThat(response.getStatusCode(), is(HttpStatus.OK));
        assertThat(response.getBody(), containsString("response_from_controller"));

        await().untilAsserted(() -> assertThat(testRepository.findAll(mainTraceId), hasSize(8)));

        lsdLogger.captureInteractionsFromDatabase(mainTraceId);
        String report = getReport("shouldRecordHeaderSuppliedNames", "Should record header supplied named - Client and TestApp");

        // Assert diagram content
        assertThat(report, containsString("Client -&gt; TestApp"));
        assertThat(report, containsString("GET /api-listener?message=from_test"));
        assertThat(report, containsString("TestApp -&gt; SomethingDoneEvent"));
        assertThat(report, containsString("publish event"));
        assertThat(report, containsString("SomethingDoneEvent -&gt; TestApp"));
        assertThat(report, containsString("consume message"));
        assertThat(report, containsString("TestApp -&gt; Client"));
        assertThat(report, containsString("200 OK response"));
        assertThat(report, containsString("TestApp -&gt; UNKNOWN_TARGET"));
        assertThat(report, containsString("POST /external-api?message=from_feign"));
        assertThat(report, containsString("UNKNOWN_TARGET -&gt; TestApp"));
        assertThat(report, containsString("200 OK response"));
        assertThat(report, containsString("TestApp -&gt; Downstream"));
        assertThat(report, containsString("POST /external-api?message=from_feign"));
        assertThat(report, containsString("Downstream -&gt; TestApp"));
        assertThat(report, containsString("200 OK response"));
    }

    @Test
    void shouldGenerateDiagramWithSuppliedNamesAndColoursForMultipleTraceIds() throws URISyntaxException {
        lsdContext.addParticipants(List.of(
                ACTOR.called("E2E"),
                PARTICIPANT.called("Setup1"),
                PARTICIPANT.called("Setup2"),
                ACTOR.called("Client"),
                PARTICIPANT.called("TestApp"),
                QUEUE.called("SomethingDoneEvent"),
                PARTICIPANT.called("UNKNOWN_TARGET"),
                PARTICIPANT.called("Downstream")
        ));
        givenExternalApi();

        ResponseEntity<String> setup1Response = sentRequest("/setup1", setupTraceId1, "E2E", "Setup1");
        assertThat(setup1Response.getStatusCode(), is(HttpStatus.OK));
        await().untilAsserted(() -> assertThat(testRepository.findAll(setupTraceId1), hasSize(2)));

        ResponseEntity<String> response = sentRequest("/api-listener", mainTraceId, "Client", "TestApp");
        assertThat(response.getStatusCode(), is(HttpStatus.OK));
        await().untilAsserted(() -> assertThat(testRepository.findAll(mainTraceId), hasSize(8)));

        ResponseEntity<String> setup2Response = sentRequest("/setup2", setupTraceId2, "E2E", "Setup2");
        assertThat(setup2Response.getStatusCode(), is(HttpStatus.OK));
        await().untilAsserted(() -> assertThat(testRepository.findAll(setupTraceId2), hasSize(2)));

        lsdLogger.captureInteractionsFromDatabase(Map.of(
                mainTraceId, Optional.of("[#blue]"),
                setupTraceId1, Optional.of("[#green]"),
                setupTraceId2, Optional.of("[#red]"))
        );

        String report = getReport("shouldGenerateDiagramWithSuppliedNamesAndColoursForMultipleTraceIds", "Should generate LSD with supplied names and colours for multiple traceIds");

        // Assert diagram content
        assertThat(report, containsString(mainTraceId));
        assertThat(report, containsString(setupTraceId1));
        assertThat(report, containsString(setupTraceId2));
        assertThat(report, containsString("blue"));
        assertThat(report, containsString("green"));
        assertThat(report, containsString("red"));
    }

    private String getReport(String title, String description) {
        lsdContext.completeScenario(title, description, OutcomeStatus.SUCCESS);
        String report = lsdContext.generateReport(title);
        lsdContext.completeReport(title);
        return report;
    }
}
