package io.lsdconsulting.lsd.distributed.interceptor.integration;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.kotlin.KotlinModule;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.matching.RequestPatternBuilder;
import com.lsdconsulting.generatorui.controller.LsdControllerStub;
import io.lsdconsulting.lsd.distributed.access.model.InteractionType;
import io.lsdconsulting.lsd.distributed.access.model.InterceptedInteraction;
import io.lsdconsulting.lsd.distributed.interceptor.integration.data.TraceIdGenerator;
import io.lsdconsulting.lsd.distributed.interceptor.integration.testapp.controller.event.SomethingDoneEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.net.URISyntaxException;

import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static io.lsdconsulting.lsd.distributed.access.model.InteractionType.*;
import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class InteractionHttpRecordingIT extends IntegrationTestBase {
    private static final String NO_BODY = "";

    @Autowired
    private RabbitTemplate rabbitTemplate;

    private final String mainTraceId = TraceIdGenerator.generate();

    private final String sourceName = randomAlphanumeric(10).toUpperCase();
    private final String targetName = randomAlphanumeric(10).toUpperCase();

    private final ObjectMapper mapper = new ObjectMapper();

    private final LsdControllerStub lsdControllerStub = new LsdControllerStub(mapper);

    @BeforeEach
    public void setup() {
        WireMock.reset();
        mapper.registerModule(new KotlinModule.Builder().build());
        mapper.registerModule(new JavaTimeModule());
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        lsdControllerStub.store(InterceptedInteraction.builder().build());
    }

    @Test
    @DisplayName("Should record interactions from RestTemplate, FeignClient and RabbitListener")
    void shouldRecordRestTemplateFeignClientAndListenerInteractions() throws URISyntaxException {
        givenExternalApi();

        final ResponseEntity<String> response = sentRequest("/api-listener", mainTraceId, null, null);

        assertThat(response.getStatusCode(), is(HttpStatus.OK));
        assertThat(response.getBody(), is("response_from_controller"));

        await().untilAsserted(() -> {
            // Assert calls to http storage endpoint
            verify(buildExpectedInterceptedInteraction(NO_BODY, "TestApp", "/api-listener?message=from_test", "/api-listener?message=from_test", null, "GET", REQUEST));
            verify(buildExpectedInterceptedInteraction("response_from_controller", "TestApp", "/api-listener?message=from_test", "/api-listener?message=from_test", "200 OK", null, RESPONSE));

            verify(buildExpectedInterceptedInteraction("{\"message\":\"from_controller\"}", "TestApp", "SomethingDoneEvent", "SomethingDoneEvent", null, null, PUBLISH));
            verify(buildExpectedInterceptedInteraction("{\"message\":\"from_controller\"}", "TestApp", "SomethingDoneEvent", "SomethingDoneEvent", null, null, CONSUME));

            verify(buildExpectedInterceptedInteraction("from_listener", "TestApp", "UNKNOWN_TARGET", "/external-api?message=from_feign", null, "POST", REQUEST));
            verify(buildExpectedInterceptedInteraction("from_external", "TestApp", "UNKNOWN_TARGET", "/external-api?message=from_feign", "200 OK", null, RESPONSE));

            verify(buildExpectedInterceptedInteraction("from_listener", "TestApp", "Downstream", "/external-api?message=from_feign", null, "POST", REQUEST));
            verify(buildExpectedInterceptedInteraction("from_external", "TestApp", "Downstream", "/external-api?message=from_feign", "200 OK", null, RESPONSE));
        });
    }

    @Test
    @DisplayName("Should record interactions with supplied names through headers")
    void shouldRecordHeaderSuppliedNames() throws URISyntaxException {
        givenExternalApi();

        final ResponseEntity<String> response = sentRequest("/api-listener", mainTraceId, sourceName, targetName);

        assertThat(response.getStatusCode(), is(HttpStatus.OK));
        assertThat(response.getBody(), containsString("response_from_controller"));

        await().untilAsserted(() -> {
            // Assert calls to http storage endpoint
            verify(buildExpectedInterceptedInteraction(NO_BODY, sourceName, targetName, "/api-listener?message=from_test", null, "GET", REQUEST));
            verify(buildExpectedInterceptedInteraction("response_from_controller", sourceName, targetName, "/api-listener?message=from_test", "200 OK", null, RESPONSE));

            verify(buildExpectedInterceptedInteraction("{\"message\":\"from_controller\"}", "TestApp", "SomethingDoneEvent", "SomethingDoneEvent", null, null, PUBLISH));
            verify(buildExpectedInterceptedInteraction("{\"message\":\"from_controller\"}", "TestApp", "SomethingDoneEvent", "SomethingDoneEvent", null, null, CONSUME));

            verify(buildExpectedInterceptedInteraction("from_listener", "TestApp", "UNKNOWN_TARGET", "/external-api?message=from_feign", null, "POST", REQUEST));
            verify(buildExpectedInterceptedInteraction("from_external", "TestApp", "UNKNOWN_TARGET", "/external-api?message=from_feign", "200 OK", null, RESPONSE));

            verify(buildExpectedInterceptedInteraction("from_listener", "TestApp", "Downstream", "/external-api?message=from_feign", null, "POST", REQUEST));
            verify(buildExpectedInterceptedInteraction("from_external", "TestApp", "Downstream", "/external-api?message=from_feign", "200 OK", null, RESPONSE));
        });
    }

    @Test
    void shouldRecordReceivingMessagesWithRabbitTemplate() throws URISyntaxException {
        final ResponseEntity<String> response = sentRequest("/api-rabbit-template", mainTraceId, sourceName, targetName);

        assertThat(response.getStatusCode(), is(HttpStatus.OK));
        assertThat(response.getBody(), is("response_from_controller"));

        final ParameterizedTypeReference<SomethingDoneEvent> type = new ParameterizedTypeReference<>() {};
        await().untilAsserted(() -> {
            final SomethingDoneEvent message = rabbitTemplate.receiveAndConvert("queue-rabbit-template", 2000, type);
            assertThat(message, is(notNullValue()));
            assertThat(message.getMessage(), is("from_controller"));
        });

        await().untilAsserted(() -> {
            // Assert calls to http storage endpoint
            verify(buildExpectedInterceptedInteraction(NO_BODY, sourceName, targetName, "/api-rabbit-template?message=from_test", null, "GET", REQUEST));
            verify(buildExpectedInterceptedInteraction("response_from_controller", sourceName, targetName, "/api-rabbit-template?message=from_test", "200 OK", null, RESPONSE));

            verify(buildExpectedInterceptedInteraction("{\"message\":\"from_controller\"}", "TestApp", "SomethingDoneEvent", "SomethingDoneEvent", null, null, PUBLISH));
            verify(buildExpectedInterceptedInteraction("{\"message\":\"from_controller\"}", "TestApp", "SomethingDoneEvent", "SomethingDoneEvent", null, null, CONSUME));
        });
    }

    @Test
    void shouldRecordObfuscatedHeaders() throws URISyntaxException {
        givenExternalApi();

        final ResponseEntity<String> response = sentRequest("/api-listener", mainTraceId, null, null);

        assertThat(response.getStatusCode(), is(HttpStatus.OK));

        await().untilAsserted(() -> {
            RequestPatternBuilder requestPatternBuilder = postRequestedFor(urlPathEqualTo("/lsds"));
            requestPatternBuilder.withRequestBody(matchingJsonPath("$.requestHeaders['Authorization']", notContaining("Password")));
            WireMock.verify(requestPatternBuilder);
        });
    }

    private InterceptedInteraction buildExpectedInterceptedInteraction(String body, String serviceName, String target, String path, String httpStatus, String httpMethod, InteractionType interactionType) {
        return InterceptedInteraction.builder()
                .traceId(mainTraceId)
                .body(body)
                .serviceName(serviceName)
                .target(target)
                .path(path)
                .httpStatus(httpStatus)
                .httpMethod(httpMethod)
                .interactionType(interactionType)
                .profile("")
                .build();
    }

    private static void verify(InterceptedInteraction interceptedInteraction) {
        RequestPatternBuilder requestPatternBuilder = postRequestedFor(urlPathEqualTo("/lsds"));
        requestPatternBuilder.withRequestBody(matchingJsonPath("$.traceId", equalTo(interceptedInteraction.getTraceId())));
        requestPatternBuilder.withRequestBody(matchingJsonPath("$.body", equalTo(interceptedInteraction.getBody())));
        requestPatternBuilder.withRequestBody(matchingJsonPath("$.serviceName", equalTo(interceptedInteraction.getServiceName())));
        requestPatternBuilder.withRequestBody(matchingJsonPath("$.target", equalTo(interceptedInteraction.getTarget())));
        requestPatternBuilder.withRequestBody(matchingJsonPath("$.path", equalTo(interceptedInteraction.getPath())));
        if (interceptedInteraction.getHttpStatus() != null) {
            requestPatternBuilder.withRequestBody(matchingJsonPath("$.httpStatus", equalTo(interceptedInteraction.getHttpStatus())));
        } else {
            requestPatternBuilder.withRequestBody(matchingJsonPath("$.httpStatus", WireMock.absent()));
        }
        if (interceptedInteraction.getHttpMethod() != null) {
            requestPatternBuilder.withRequestBody(matchingJsonPath("$.httpMethod", equalTo(interceptedInteraction.getHttpMethod())));
        } else {
            requestPatternBuilder.withRequestBody(matchingJsonPath("$.httpMethod", WireMock.absent()));
        }
        requestPatternBuilder.withRequestBody(matchingJsonPath("$.interactionType", equalTo(interceptedInteraction.getInteractionType().name())));
        requestPatternBuilder.withRequestBody(matchingJsonPath("$.profile", equalTo(interceptedInteraction.getProfile())));
        WireMock.verify(requestPatternBuilder);
    }
}
