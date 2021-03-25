package com.integreety.yatspec.e2e.integration;

import com.integreety.yatspec.e2e.captor.repository.model.InterceptedInteraction;
import com.integreety.yatspec.e2e.teststate.interaction.InteractionNameGenerator;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import wiremock.org.apache.commons.lang3.RandomStringUtils;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import static com.integreety.yatspec.e2e.captor.repository.model.Type.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.params.provider.Arguments.of;

public class InteractionNameGeneratorIT {

    private static final String TRACE_ID = RandomStringUtils.randomAlphabetic(10);
    private static final String BODY = "{\"key\":\"value\"}";

    private final InteractionNameGenerator underTest = new InteractionNameGenerator();

    @ParameterizedTest
    @MethodSource("provideInterceptedInteractions")
    public void shouldGenerateInteractionNames(final InterceptedInteraction interceptedInteraction, final String expectedInteractionName) {
        final List<Pair<String, Object>> interactionNames = underTest.generate(List.of(interceptedInteraction), Map.of(TRACE_ID, Optional.of("grey")));

        assertThat(interactionNames, hasSize(1));
        assertThat(interactionNames.get(0).getLeft(), is(expectedInteractionName));
    }

    private static Stream<Arguments> provideInterceptedInteractions() {
        return Stream.of(
                of(InterceptedInteraction.builder().traceId(TRACE_ID).target("/abc/def").serviceName("service").body(BODY).type(REQUEST).httpMethod("POST").build(), "POST /abc/def from service to /abc/def"),
                of(InterceptedInteraction.builder().traceId(TRACE_ID).target("/abc/def").serviceName("service").body(BODY).type(REQUEST).httpMethod("POST").requestHeaders(Map.of("Target-Name", List.of("Arnie"))).build(), "POST /abc/def from service to Arnie"),
                of(InterceptedInteraction.builder().traceId(TRACE_ID).target("/abc/def").serviceName("service").body(BODY).type(REQUEST).httpMethod("POST").requestHeaders(Map.of("Source-Name", List.of("Jean"))).build(), "POST /abc/def from Jean to /abc/def"),
                of(InterceptedInteraction.builder().traceId(TRACE_ID).target("/abc/def").serviceName("service").body(BODY).type(REQUEST).httpMethod("POST").requestHeaders(Map.of("Source-Name", List.of("Jean"), "Target-Name", List.of("Arnie"))).build(), "POST /abc/def from Jean to Arnie"),
                of(InterceptedInteraction.builder().traceId(TRACE_ID).target("/abc/def").serviceName("service").body(BODY).type(RESPONSE).httpStatus("200").build(), "200 response from /abc/def to service"),
                of(InterceptedInteraction.builder().traceId(TRACE_ID).target("/abc/def").serviceName("service").body(BODY).type(RESPONSE).httpStatus("200").requestHeaders(Map.of("Source-Name", List.of("Jean"), "Target-Name", List.of("Arnie"))).build(), "200 response from Arnie to Jean"),
                of(InterceptedInteraction.builder().traceId(TRACE_ID).target("exchange").serviceName("service").body(BODY).type(PUBLISH).build(), "publish event from service to exchange"),
                of(InterceptedInteraction.builder().traceId(TRACE_ID).target("exchange").serviceName("service").body(BODY).type(CONSUME).build(), "consume message from exchange to service")
        );
    }
}
