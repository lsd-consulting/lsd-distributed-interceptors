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
import org.springframework.http.HttpStatus;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import static com.integreety.yatspec.e2e.captor.repository.model.Type.*;
import static com.integreety.yatspec.e2e.teststate.TraceIdGenerator.generate;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.RequestEntity.get;

@Slf4j
@SpringBootTest(webEnvironment = RANDOM_PORT, classes = {TestApplication.class})
@SpringJUnitConfig(classes = EndToEndConfiguration.class)
@TestPropertySource("classpath:application-test.properties")
@Execution(ExecutionMode.SAME_THREAD)
public class EndToEndIT {

    @LocalServerPort
    private int serverPort;

    @Autowired
    private TestRestTemplate testRestTemplate;

    private final String traceId = generate();
    private final TestRepository testRepository = new TestRepository();

    @Test
    public void shouldRecordAllInteractions() throws URISyntaxException {
        final ResponseEntity<String> response = sendInitialRequest(traceId);

        assertThat(response.getStatusCode(), is(HttpStatus.OK));
        assertThat(response.getBody(), containsString("OK"));

        await().untilAsserted(() -> {
            final List<InterceptedCall> interceptedCalls = testRepository.findAll(traceId);
            assertThat(interceptedCalls, hasSize(4));
            assertThat("CONSUMER interaction missing", interceptedCalls, hasItem(hasProperty("type", is(CONSUME))));
            assertThat("PUBLISH interaction missing", interceptedCalls, hasItem(hasProperty("type", is(PUBLISH))));
            assertThat("REQUEST interaction missing", interceptedCalls, hasItem(hasProperty("type", is(REQUEST))));
            assertThat("RESPONSE interaction missing", interceptedCalls, hasItem(hasProperty("type", is(RESPONSE))));
        });
    }

    @Test
    public void shouldRecordServiceNames() throws URISyntaxException {
        final ResponseEntity<String> response = sendInitialRequest(traceId);

        assertThat(response.getStatusCode(), is(HttpStatus.OK));
        assertThat(response.getBody(), containsString("OK"));

        await().untilAsserted(() -> {
            final List<InterceptedCall> interceptedCalls = testRepository.findAll(traceId);
            assertThat(interceptedCalls, hasSize(4));
            assertThat("Wrong service name recorded", interceptedCalls, hasItem(hasProperty("serviceName", is("lsdEnd2End"))));
            assertThat("Wrong service name recorded", interceptedCalls, hasItem(hasProperty("serviceName", is("lsdEnd2End"))));
            assertThat("Wrong service name recorded", interceptedCalls, hasItem(hasProperty("serviceName", is("lsdEnd2End"))));
            assertThat("Wrong service name recorded", interceptedCalls, hasItem(hasProperty("serviceName", is("lsdEnd2End"))));
        });
    }

    @Test
    public void shouldRecordServiceNames2() throws URISyntaxException {
        final ResponseEntity<String> response = sendInitialRequest(traceId);

        assertThat(response.getStatusCode(), is(HttpStatus.OK));
        assertThat(response.getBody(), containsString("OK"));

        await().untilAsserted(() -> {
            final List<InterceptedCall> interceptedCalls = testRepository.findAll(traceId);
            assertThat(interceptedCalls, hasSize(4));
            assertThat("Wrong service name recorded", interceptedCalls, hasItem(hasProperty("serviceName", is("lsdEnd2End"))));
            assertThat("Wrong service name recorded", interceptedCalls, hasItem(hasProperty("serviceName", is("lsdEnd2End"))));
            assertThat("Wrong service name recorded", interceptedCalls, hasItem(hasProperty("serviceName", is("lsdEnd2End"))));
            assertThat("Wrong service name recorded", interceptedCalls, hasItem(hasProperty("serviceName", is("lsdEnd2End"))));
        });
    }

    public ResponseEntity<String> sendInitialRequest(final String traceId) throws URISyntaxException {
        log.info("Sending traceId:{}", traceId);
        final RequestEntity<?> requestEntity = get(new URI("http://localhost:" + serverPort + "/objects?message=OK"))
                .header("Content-Type", APPLICATION_JSON_VALUE)
                .header("b3", traceId + "-" + traceId + "-1")
                .build();

        return testRestTemplate.exchange(requestEntity, String.class);
    }
}
