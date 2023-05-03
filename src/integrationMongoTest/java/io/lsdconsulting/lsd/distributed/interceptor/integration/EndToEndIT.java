package io.lsdconsulting.lsd.distributed.interceptor.integration;

import com.lsd.core.LsdContext;
import io.lsdconsulting.lsd.distributed.generator.diagram.InteractionGenerator;
import io.lsdconsulting.lsd.distributed.generator.diagram.LsdLogger;
import io.lsdconsulting.lsd.distributed.interceptor.integration.data.TraceIdGenerator;
import io.lsdconsulting.lsd.distributed.interceptor.integration.testapp.repository.TestRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;

import static com.lsd.core.domain.ParticipantType.*;
import static com.lsd.core.domain.Status.SUCCESS;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class EndToEndIT extends IntegrationTestBase {

    @Autowired
    private TestRepository testRepository;

    @Autowired
    private InteractionGenerator interactionGenerator;

    private final LsdContext lsdContext = new LsdContext();
    private final String mainTraceId = TraceIdGenerator.generate();
    private final String setupTraceId1 = TraceIdGenerator.generate();
    private final String setupTraceId2 = TraceIdGenerator.generate();

    private LsdLogger lsdLogger;

    @BeforeEach
    void setup() {
        lsdLogger = new LsdLogger(interactionGenerator);
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

        lsdLogger.captureInteractionsFromDatabase(lsdContext, mainTraceId);
        String report = getReport("shouldRecordHeaderSuppliedNames", "Should record header supplied names - Client and TestApp");

        // Assert diagram content
        assertThat(report, containsString("Client -&gt; TestApp"));
        assertThat(report, containsString("GET /api-listener?message=from_test"));
        assertThat(report, containsString("TestApp -&gt; SomethingDoneEvent"));
        assertThat(report, containsString("publish event"));
        assertThat(report, containsString("SomethingDoneEvent -&gt; TestApp"));
        assertThat(report, containsString("consume message"));
        assertThat(report, containsString("TestApp --&gt; Client"));
        assertThat(report, matchesPattern("(?s).*\"sync 200 OK response \\([0-9]+ ms\\)\"(?s).*"));
        assertThat(report, containsString("TestApp -&gt; UNKNOWN_TARGET"));
        assertThat(report, containsString("POST /external-api?message=from_feign"));
        assertThat(report, containsString("UNKNOWN_TARGET --&gt; TestApp"));
        assertThat(report, matchesPattern("(?s).*\"sync 200 OK response \\([0-9]+ ms\\)\"(?s).*"));
        assertThat(report, containsString("TestApp -&gt; Downstream"));
        assertThat(report, containsString("POST /external-api?message=from_feign"));
        assertThat(report, containsString("Downstream --&gt; TestApp"));
        assertThat(report, matchesPattern("(?s).*\"sync 200 OK response \\([0-9]+ ms\\)\"(?s).*"));
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

        lsdLogger.captureInteractionsFromDatabase(lsdContext, Map.of(
                mainTraceId, "blue",
                setupTraceId1, "green",
                setupTraceId2, "red")
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
        lsdContext.completeScenario(title, description, SUCCESS);
        String report = lsdContext.renderReport(title);
        lsdContext.completeReport(title);
        return report;
    }
}
