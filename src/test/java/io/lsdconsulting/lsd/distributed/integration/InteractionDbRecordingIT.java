package io.lsdconsulting.lsd.distributed.integration;

import io.lsdconsulting.lsd.distributed.captor.repository.model.InterceptedInteraction;
import io.lsdconsulting.lsd.distributed.diagram.TraceIdGenerator;
import io.lsdconsulting.lsd.distributed.integration.testapp.TestApplication;
import io.lsdconsulting.lsd.distributed.integration.testapp.config.RabbitConfig;
import io.lsdconsulting.lsd.distributed.integration.testapp.config.RabbitTemplateConfig;
import io.lsdconsulting.lsd.distributed.integration.testapp.config.RepositoryConfig;
import io.lsdconsulting.lsd.distributed.integration.testapp.config.RestConfig;
import io.lsdconsulting.lsd.distributed.integration.testapp.controller.event.SomethingDoneEvent;
import io.lsdconsulting.lsd.distributed.integration.testapp.repository.TestRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import static io.lsdconsulting.lsd.distributed.captor.repository.model.Type.*;
import static io.lsdconsulting.lsd.distributed.integration.matcher.InterceptedInteractionMatcher.with;
import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@Slf4j
@SpringJUnitConfig(classes = {RepositoryConfig.class, RestConfig.class, RabbitConfig.class, RabbitTemplateConfig.class})
@SpringBootTest(webEnvironment = RANDOM_PORT, classes = {TestApplication.class})
@TestPropertySource("classpath:application-test.properties")
@AutoConfigureWireMock(port = 0)
public class InteractionDbRecordingIT extends IntegrationTestBase {

    private static final String NO_BODY = "";

    @Autowired
    private TestRepository testRepository;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    private final String mainTraceId = TraceIdGenerator.generate();

    private final String sourceName = randomAlphanumeric(10).toUpperCase();
    private final String targetName = randomAlphanumeric(10).toUpperCase();

    @Test
    @DisplayName("Should record interactions from RestTemplate, FeignClient and RabbitListener")
    void shouldRecordRestTemplateFeignClientAndListenerInteractions() throws URISyntaxException {
        givenExternalApi();

        final ResponseEntity<String> response = sentRequest("/api-listener", mainTraceId, null, null);

        assertThat(response.getStatusCode(), is(HttpStatus.OK));
        assertThat(response.getBody(), is("response_from_controller"));

        final List<InterceptedInteraction> interceptedInteractions = new ArrayList<>();
        await().untilAsserted(() -> {
            final List<InterceptedInteraction> foundInterceptedInteractions = testRepository.findAll(mainTraceId);
            assertThat(foundInterceptedInteractions, hasSize(8));
            interceptedInteractions.addAll(foundInterceptedInteractions);
        });

        // Assert db state
        assertThat("REQUEST interaction missing", interceptedInteractions, hasItem(with(REQUEST, "TestApp", NO_BODY, "/api-listener?message=from_test", "/api-listener?message=from_test")));
        assertThat("REQUEST interaction missing", interceptedInteractions, hasItem(with(RESPONSE, "TestApp", "response_from_controller", "/api-listener?message=from_test", "/api-listener?message=from_test")));

        assertThat("PUBLISH interaction missing", interceptedInteractions, hasItem(with(PUBLISH, "TestApp", "{\"message\":\"from_controller\"}", "SomethingDoneEvent", "SomethingDoneEvent")));
        assertThat("CONSUMER interaction missing", interceptedInteractions, hasItem(with(CONSUME, "TestApp", "{\"message\":\"from_controller\"}", "SomethingDoneEvent", "SomethingDoneEvent")));

        assertThat("REQUEST interaction missing", interceptedInteractions, hasItem(with(REQUEST, "TestApp", "from_listener", "UNKNOWN_TARGET", "/external-api?message=from_feign")));
        assertThat("REQUEST interaction missing", interceptedInteractions, hasItem(with(RESPONSE, "TestApp", "from_external", "UNKNOWN_TARGET", "/external-api?message=from_feign")));

        assertThat("REQUEST interaction missing", interceptedInteractions, hasItem(with(REQUEST, "TestApp", "from_listener", "Downstream", "/external-api?message=from_feign")));
        assertThat("REQUEST interaction missing", interceptedInteractions, hasItem(with(RESPONSE, "TestApp", "from_external", "Downstream", "/external-api?message=from_feign")));
    }


    @Test
    void shouldRecordHeaderSuppliedNames() throws URISyntaxException {
        givenExternalApi();

        final ResponseEntity<String> response = sentRequest("/api-listener", mainTraceId, sourceName, targetName);

        assertThat(response.getStatusCode(), is(HttpStatus.OK));
        assertThat(response.getBody(), containsString("response_from_controller"));

        final List<InterceptedInteraction> interceptedInteractions = new ArrayList<>();
        await().untilAsserted(() -> {
            final List<InterceptedInteraction> foundInterceptedInteractions = testRepository.findAll(mainTraceId);
            assertThat(foundInterceptedInteractions, hasSize(8));
            interceptedInteractions.addAll(foundInterceptedInteractions);
        });

        // Assert db state
        assertThat("REQUEST interaction missing", interceptedInteractions, hasItem(with(REQUEST, sourceName, NO_BODY, targetName, "/api-listener?message=from_test")));
        assertThat("REQUEST interaction missing", interceptedInteractions, hasItem(with(RESPONSE, sourceName, "response_from_controller", targetName, "/api-listener?message=from_test")));

        assertThat("PUBLISH interaction missing", interceptedInteractions, hasItem(with(PUBLISH, "TestApp", "{\"message\":\"from_controller\"}", "SomethingDoneEvent", "SomethingDoneEvent")));
        assertThat("CONSUMER interaction missing", interceptedInteractions, hasItem(with(CONSUME, "TestApp", "{\"message\":\"from_controller\"}", "SomethingDoneEvent", "SomethingDoneEvent")));

        assertThat("REQUEST interaction missing", interceptedInteractions, hasItem(with(REQUEST, "TestApp", "from_listener", "UNKNOWN_TARGET", "/external-api?message=from_feign")));
        assertThat("REQUEST interaction missing", interceptedInteractions, hasItem(with(RESPONSE, "TestApp", "from_external", "UNKNOWN_TARGET", "/external-api?message=from_feign")));

        assertThat("REQUEST interaction missing", interceptedInteractions, hasItem(with(REQUEST, "TestApp", "from_listener", "Downstream", "/external-api?message=from_feign")));
        assertThat("REQUEST interaction missing", interceptedInteractions, hasItem(with(RESPONSE, "TestApp", "from_external", "Downstream", "/external-api?message=from_feign")));
    }

    @Test
    void shouldRecordReceivingMessagesWithRabbitTemplate() throws URISyntaxException {
        final ResponseEntity<String> response = sentRequest("/api-rabbit-template", mainTraceId, sourceName, targetName);

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

        assertThat("REQUEST interaction missing", interceptedInteractions, hasItem(with(REQUEST, sourceName, NO_BODY, targetName, "/api-rabbit-template?message=from_test")));
        assertThat("REQUEST interaction missing", interceptedInteractions, hasItem(with(RESPONSE, sourceName, "response_from_controller", targetName, "/api-rabbit-template?message=from_test")));

        assertThat("PUBLISH interaction missing", interceptedInteractions, hasItem(with(PUBLISH, "TestApp", "{\"message\":\"from_controller\"}", "SomethingDoneEvent", "SomethingDoneEvent")));
        assertThat("CONSUMER interaction missing", interceptedInteractions, hasItem(with(CONSUME, "TestApp", "{\"message\":\"from_controller\"}", "SomethingDoneEvent", "SomethingDoneEvent")));
    }

    @Test
    void shouldRecordObfuscatedHeaders() throws URISyntaxException {
        givenExternalApi();

        final ResponseEntity<String> response = sentRequest("/api-listener", mainTraceId, null, null);

        assertThat(response.getStatusCode(), is(HttpStatus.OK));

        final List<InterceptedInteraction> interceptedInteractions = new ArrayList<>();
        await().untilAsserted(() -> {
            final List<InterceptedInteraction> foundInterceptedInteractions = testRepository.findAll(mainTraceId);
            assertThat(foundInterceptedInteractions, hasSize(8));
            interceptedInteractions.addAll(foundInterceptedInteractions);
        });

        assertThat("Header obfuscation did not work ", interceptedInteractions, not(hasItem(hasProperty("requestHeaders", hasEntry("Authorization", List.of("Password"))))));
    }
}
