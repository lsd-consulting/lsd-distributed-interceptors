package com.integreety.yatspec.e2e.integration;

import com.integreety.yatspec.e2e.captor.repository.model.InterceptedCall;
import com.integreety.yatspec.e2e.teststate.interaction.InteractionNameGenerator;
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

public class InteractionNameGeneratorIT {

    private final InteractionNameGenerator underTest = new InteractionNameGenerator();

    @ParameterizedTest
    @MethodSource("provideInterceptedCalls")
    public void shouldGenerateRequestInteractionNames(final InterceptedCall interceptedCall, final String expectedInteractionName) {
        final List<Pair<String, Object>> interactionNames = underTest.generate(userSuppliedSourceMappings(Map.of()), userSuppliedDestinationMappings(Map.of()), List.of(interceptedCall));

        assertThat(interactionNames, hasSize(1));
        assertThat(interactionNames.get(0).getLeft(), is(expectedInteractionName));
    }

    private static Stream<Arguments> provideInterceptedCalls() {
        return Stream.of(
                Arguments.of(InterceptedCall.builder()
                                .target("/someResource/someChildResource")
                                .serviceName("serviceName")
                                .httpMethod("POST")
                                .body("{\"key\":\"value\"}")
                                .type(REQUEST)
                                .build(),
                        "POST /someResource/someChildResource from serviceName to someResource"),

                Arguments.of(InterceptedCall.builder()
                                .httpStatus("200")
                                .target("/someResource/someChildResource")
                                .serviceName("serviceName")
                                .body("{\"key\":\"value\"}")
                                .type(RESPONSE)
                                .build(),
                        "200 response from someResource to serviceName"),

                Arguments.of(InterceptedCall.builder()
                                .target("someExchange")
                                .serviceName("serviceName")
                                .body("{\"key\":\"value\"}")
                                .type(PUBLISH)
                                .build(),
                        "publish event from serviceName to someExchange"),

                Arguments.of(InterceptedCall.builder()
                                .target("someExchange")
                                .serviceName("serviceName")
                                .body("{\"key\":\"value\"}")
                                .type(CONSUME)
                                .build(),
                        "consume message from someExchange to serviceName")
        );
    }
}