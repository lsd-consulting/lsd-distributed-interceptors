package com.integreety.yatspec.e2e.teststate.interaction;

import com.integreety.yatspec.e2e.captor.repository.model.InterceptedInteraction;
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

public class InteractionNameGeneratorShould {

    private static final String TRACE_ID = RandomStringUtils.randomAlphabetic(10);

    private final InteractionNameGenerator underTest = new InteractionNameGenerator();

    @ParameterizedTest
    @MethodSource("provideInterceptedInteractions")
    public void generateInteractionNames(final InterceptedInteraction interceptedInteraction, final String expectedInteractionName) {
        final List<Pair<String, Object>> interactionNames = underTest.generate(List.of(interceptedInteraction), Map.of(TRACE_ID, Optional.of("[#grey]")));

        assertThat(interactionNames, hasSize(1));
        assertThat(interactionNames.get(0).getLeft(), is(expectedInteractionName));
    }

    private static Stream<Arguments> provideInterceptedInteractions() {
        return Stream.of(
                of(InterceptedInteraction.builder().traceId(TRACE_ID).path("/abc/def").target("target").serviceName("service").type(REQUEST).httpMethod("POST").build(), "POST /abc/def from service to target [#grey]"),
                of(InterceptedInteraction.builder().traceId(TRACE_ID).target("target").serviceName("service").type(RESPONSE).httpStatus("200").build(), "200 response from target to service [#grey]"),
                of(InterceptedInteraction.builder().traceId(TRACE_ID).target("exchange").serviceName("service").type(PUBLISH).build(), "publish event from service to exchange [#grey]"),
                of(InterceptedInteraction.builder().traceId(TRACE_ID).target("exchange").serviceName("service").type(CONSUME).build(), "consume message from exchange to service [#grey]")
        );
    }
}
