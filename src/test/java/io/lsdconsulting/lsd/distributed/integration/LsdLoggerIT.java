package io.lsdconsulting.lsd.distributed.integration;

import com.lsd.LsdContext;
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
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.net.URISyntaxException;
import java.util.Map;
import java.util.Optional;

import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.spy;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@Slf4j
@SpringJUnitConfig(classes = {RepositoryConfig.class, RestConfig.class, RabbitConfig.class, RabbitTemplateConfig.class})
@SpringBootTest(webEnvironment = RANDOM_PORT, classes = {TestApplication.class})
@TestPropertySource("classpath:application-test.properties")
@AutoConfigureWireMock(port = 0)
public class LsdLoggerIT extends IntegrationTestBase {

    @Autowired
    private TestRepository testRepository;

    @Autowired
    private InterceptedDocumentRepository interceptedDocumentRepository;

    @Autowired
    private InteractionGenerator interactionGenerator;

    private final String setupTraceId = TraceIdGenerator.generate();
    private final String mainTraceId = TraceIdGenerator.generate();
    private final String sourceName = randomAlphanumeric(10).toUpperCase();
    private final String targetName = randomAlphanumeric(10).toUpperCase();

    private final LsdContext realContext = new LsdContext();
    private final LsdContext lsdContext = spy(realContext);
    private final ArgumentCaptor<String> argumentCaptor = ArgumentCaptor.forClass(String.class);

    private LsdLogger lsdLogger;

    @BeforeEach
    void setup() {
        lsdLogger = new LsdLogger(interceptedDocumentRepository, interactionGenerator, lsdContext);
    }

    @Test
    void shouldLogInteractionsInLsdContextWithSuppliedNamesAndColourForTraceId() throws URISyntaxException {
        doNothing().when(lsdContext).capture(argumentCaptor.capture(), any());
        givenExternalApi();

        final ResponseEntity<String> response = sentRequest("/api-listener", mainTraceId, sourceName, targetName);
        assertThat(response.getStatusCode(), is(HttpStatus.OK));
        assertThat(response.getBody(), containsString("response_from_controller"));

        await().untilAsserted(() -> assertThat(testRepository.findAll(mainTraceId), hasSize(8)));

        lsdLogger.captureInteractionsFromDatabase(Map.of(mainTraceId, Optional.of("[#blue]")));

        assertThat(argumentCaptor.getAllValues(), containsInAnyOrder(
                "GET /api-listener?message=from_test from " + sourceName + " to " + targetName + " [#blue]",
                "publish event from TestApp to SomethingDoneEvent [#blue]",
                "200 OK response from " + targetName + " to " + sourceName + " [#blue]",
                "consume message from SomethingDoneEvent to TestApp [#blue]",
                "POST /external-api?message=from_feign from TestApp to UNKNOWN_TARGET [#blue]",
                "200 OK response from UNKNOWN_TARGET to TestApp [#blue]",
                "POST /external-api?message=from_feign from TestApp to Downstream [#blue]",
                "200 OK response from Downstream to TestApp [#blue]"));
    }

    @Test
    void shouldLogInteractionsInLsdContextWithSuppliedNamesAndColoursForMultipleTraceIds() throws URISyntaxException {
        doNothing().when(lsdContext).capture(argumentCaptor.capture(), any());

        givenExternalApi();

        sentRequest("/setup1", setupTraceId, "E2E", "Setup1");

        final ResponseEntity<String> response = sentRequest("/api-listener", mainTraceId, sourceName, targetName);

        assertThat(response.getStatusCode(), is(HttpStatus.OK));
        assertThat(response.getBody(), containsString("response_from_controller"));

        await().untilAsserted(() -> assertThat(testRepository.findAll(mainTraceId), hasSize(8)));

        sentRequest("/setup2", setupTraceId, "E2E", "Setup2");

        lsdLogger.captureInteractionsFromDatabase(Map.of(mainTraceId, Optional.of("[#blue]"), setupTraceId, Optional.of("[#green]")));

        assertThat(argumentCaptor.getAllValues(), containsInAnyOrder(
                "GET /setup1?message=from_test from E2E to Setup1 [#green]",
                "200 OK response from Setup1 to E2E [#green]",
                "GET /api-listener?message=from_test from " + sourceName + " to " + targetName + " [#blue]",
                "publish event from TestApp to SomethingDoneEvent [#blue]",
                "200 OK response from " + targetName + " to " + sourceName + " [#blue]",
                "consume message from SomethingDoneEvent to TestApp [#blue]",
                "POST /external-api?message=from_feign from TestApp to UNKNOWN_TARGET [#blue]",
                "200 OK response from UNKNOWN_TARGET to TestApp [#blue]",
                "POST /external-api?message=from_feign from TestApp to Downstream [#blue]",
                "200 OK response from Downstream to TestApp [#blue]",
                "GET /setup2?message=from_test from E2E to Setup2 [#green]",
                "200 OK response from Setup2 to E2E [#green]"));
    }
}
