package com.integreety.yatspec.e2e.integration;

import com.integreety.yatspec.e2e.captor.repository.model.InterceptedCall;
import com.integreety.yatspec.e2e.teststate.interaction.InteractionNameGenerator;
import com.integreety.yatspec.e2e.teststate.mapper.destination.DestinationNameMappings;
import com.integreety.yatspec.e2e.teststate.mapper.source.SourceNameMappings;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static com.integreety.yatspec.e2e.captor.repository.model.Type.*;
import static com.integreety.yatspec.e2e.teststate.mapper.destination.UserSuppliedDestinationMappings.userSuppliedDestinationMappings;
import static com.integreety.yatspec.e2e.teststate.mapper.source.UserSuppliedSourceMappings.userSuppliedSourceMappings;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.params.provider.Arguments.of;

public class InteractionNameGeneratorIT {

    private static final String BODY = "{\"key\":\"value\"}";
    private final InteractionNameGenerator underTest = new InteractionNameGenerator();

    @ParameterizedTest
    @MethodSource("provideInterceptedCalls")
    public void shouldGenerateInteractionNames(final InterceptedCall interceptedCall, final String expectedInteractionName) {
        final List<Pair<String, Object>> interactionNames = underTest.generate(userSuppliedSourceMappings(Map.of()), userSuppliedDestinationMappings(Map.of()), List.of(interceptedCall));

        assertThat(interactionNames, hasSize(1));
        assertThat(interactionNames.get(0).getLeft(), is(expectedInteractionName));
    }

    @ParameterizedTest
    @MethodSource("provideInterceptedCallsWithSourceMappings")
    public void shouldGenerateInteractionNamesUsingUserSuppliedSourceMappings(final InterceptedCall interceptedCall, final String expectedInteractionName) {
        final Pair<String, String> source1 = Pair.of("service", "/abc/def");
        final Pair<String, String> source2 = Pair.of("service", "exchange");
        final SourceNameMappings sourceNameMappings = userSuppliedSourceMappings(Map.of(source1, "source1", source2, "source2"));
        final DestinationNameMappings destinationNameMappings = userSuppliedDestinationMappings(Map.of());
        final List<Pair<String, Object>> interactionNames = underTest.generate(sourceNameMappings, destinationNameMappings, List.of(interceptedCall));

        assertThat(interactionNames, hasSize(1));
        assertThat(interactionNames.get(0).getLeft(), is(expectedInteractionName));
    }

    @ParameterizedTest
    @MethodSource("provideInterceptedCallsWithDestinationMappings")
    public void shouldGenerateInteractionNamesUsingUserSuppliedDestinationMappings(final InterceptedCall interceptedCall, final String expectedInteractionName) {
        final SourceNameMappings sourceNameMappings = userSuppliedSourceMappings(Map.of());
        final DestinationNameMappings destinationNameMappings = userSuppliedDestinationMappings(Map.of("/abc/def", "dest1", "exchange", "dest2"));
        final List<Pair<String, Object>> interactionNames = underTest.generate(sourceNameMappings, destinationNameMappings, List.of(interceptedCall));

        assertThat(interactionNames, hasSize(1));
        assertThat(interactionNames.get(0).getLeft(), is(expectedInteractionName));
    }

    private static Stream<Arguments> provideInterceptedCalls() {
        return Stream.of(
                of(InterceptedCall.builder().target("/abc/def").serviceName("service").body(BODY).type(REQUEST).httpMethod("POST").build(), "POST /abc/def from service to abc"),
                of(InterceptedCall.builder().target("/abc/def").serviceName("service").body(BODY).type(RESPONSE).httpStatus("200").build(), "200 response from abc to service"),
                of(InterceptedCall.builder().target("exchange").serviceName("service").body(BODY).type(PUBLISH).build(), "publish event from service to exchange"),
                of(InterceptedCall.builder().target("exchange").serviceName("service").body(BODY).type(CONSUME).build(), "consume message from exchange to service")
        );
    }

    private static Stream<Arguments> provideInterceptedCallsWithSourceMappings() {
        return Stream.of(
                of(InterceptedCall.builder().target("/abc/def").serviceName("service").body(BODY).type(REQUEST).httpMethod("POST").build(), "POST /abc/def from source1 to abc"),
                of(InterceptedCall.builder().target("/abc/def").serviceName("service").body(BODY).type(RESPONSE).httpStatus("200").build(), "200 response from abc to source1"),
                of(InterceptedCall.builder().target("exchange").serviceName("service").body(BODY).type(PUBLISH).build(), "publish event from source2 to exchange"),
                of(InterceptedCall.builder().target("exchange").serviceName("service").body(BODY).type(CONSUME).build(), "consume message from exchange to source2")
        );
    }

    private static Stream<Arguments> provideInterceptedCallsWithDestinationMappings() {
        return Stream.of(
                of(InterceptedCall.builder().target("/abc/def").serviceName("service").body(BODY).type(REQUEST).httpMethod("POST").build(), "POST /abc/def from service to dest1"),
                of(InterceptedCall.builder().target("/abc/def").serviceName("service").body(BODY).type(RESPONSE).httpStatus("200").build(), "200 response from dest1 to service"),
                of(InterceptedCall.builder().target("exchange").serviceName("service").body(BODY).type(PUBLISH).build(), "publish event from service to dest2"),
                of(InterceptedCall.builder().target("exchange").serviceName("service").body(BODY).type(CONSUME).build(), "consume message from dest2 to service")
        );
    }
}