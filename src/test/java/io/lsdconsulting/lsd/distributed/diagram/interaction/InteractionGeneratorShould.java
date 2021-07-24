package io.lsdconsulting.lsd.distributed.diagram.interaction;

import io.lsdconsulting.lsd.distributed.captor.repository.model.InterceptedInteraction;
import io.lsdconsulting.lsd.distributed.captor.repository.model.Type;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import static io.lsdconsulting.lsd.distributed.captor.repository.model.Type.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.params.provider.Arguments.of;
import static wiremock.org.apache.commons.lang3.RandomStringUtils.randomAlphabetic;

class InteractionGeneratorShould {

    private static final String TRACE_ID = randomAlphabetic(10);

    private final InteractionGenerator underTest = new InteractionGenerator();

    @ParameterizedTest
    @MethodSource("provideInterceptedInteractions")
    void generateInteractions(final InterceptedInteraction interceptedInteraction, final String expectedInteractionName, final String expectedBody) {
        final List<Pair<String, String>> interactionNames = underTest.generate(List.of(interceptedInteraction), Map.of(TRACE_ID, Optional.of("[#grey]")));

        assertThat(interactionNames, hasSize(1));
        assertThat(interactionNames.get(0).getLeft(), is(expectedInteractionName));
        assertThat(interactionNames.get(0).getRight(), is(expectedBody));
    }

    private static Stream<Arguments> provideInterceptedInteractions() {
        return Stream.of(
                of(InterceptedInteraction.builder().traceId(TRACE_ID).path("/abc/def").target("target").serviceName("service").type(REQUEST).httpMethod("POST").body("key1=value1;key2=value2").build(), "POST /abc/def from Service to Target [#grey]", "{\n  \"body\": \"key1=value1;key2=value2\"\n}"),
                of(InterceptedInteraction.builder().traceId(TRACE_ID).target("target").serviceName("service").type(RESPONSE).httpStatus("200").body("someValue").build(), "200 response from Target to Service [#grey]", "{\n  \"body\": \"someValue\"\n}"),
                of(InterceptedInteraction.builder().traceId(TRACE_ID).target("exchange").serviceName("service").type(PUBLISH).body("{\"key1\":\"value1\",\"key2\":\"value2\"}").build(), "publish event from Service to Exchange [#grey]", "{\n  \"body\": \"{\\\"key1\\\":\\\"value1\\\",\\\"key2\\\":\\\"value2\\\"}\"\n}"),
                of(InterceptedInteraction.builder().traceId(TRACE_ID).target("exchange").serviceName("service").type(CONSUME).body("").build(), "consume message from Exchange to Service [#grey]", "{\n  \"body\": \"\"\n}")
        );
    }

    @Test
    void generateRequestHeadersInBody() {
        InterceptedInteraction interceptedInteraction = buildInterceptedInteraction(REQUEST)
                .requestHeaders(Map.of("header", List.of("value")))
                .build();


        final List<Pair<String, String>> interactionNames = underTest.generate(List.of(interceptedInteraction), Map.of());

        String body = interactionNames.get(0).getRight();

        assertThat(body, containsString("requestHeaders"));
        assertThat(body, not(containsString("responseHeaders")));
        assertThat(body, not(containsString("headers")));
    }

    @Test
    void generateResponseHeadersInBody() {
        InterceptedInteraction interceptedInteraction = buildInterceptedInteraction(RESPONSE)
                .responseHeaders(Map.of("header", List.of("value")))
                .build();

        final List<Pair<String, String>> interactionNames = underTest.generate(List.of(interceptedInteraction), Map.of());

        String body = interactionNames.get(0).getRight();

        assertThat(body, containsString("responseHeaders"));
        assertThat(body, not(containsString("requestHeaders")));
        assertThat(body, not(containsString("headers")));
    }

    @Test
    void generateHeadersInBody() {
        InterceptedInteraction interceptedInteraction = buildInterceptedInteraction(PUBLISH)
                .requestHeaders(Map.of("header", List.of("value")))
                .build();

        final List<Pair<String, String>> interactionNames = underTest.generate(List.of(interceptedInteraction), Map.of());

        String body = interactionNames.get(0).getRight();

        assertThat(body, containsString("headers"));
        assertThat(body, not(containsString("responseHeaders")));
        assertThat(body, not(containsString("requestHeaders")));
    }

    private InterceptedInteraction.InterceptedInteractionBuilder buildInterceptedInteraction(Type type) {
        return InterceptedInteraction.builder()
                .traceId(TRACE_ID)
                .type(type)
                .target("target")
                .serviceName("service");
    }
}
