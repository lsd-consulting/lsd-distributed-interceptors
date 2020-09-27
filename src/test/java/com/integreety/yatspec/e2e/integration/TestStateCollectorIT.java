package com.integreety.yatspec.e2e.integration;

import com.googlecode.yatspec.state.givenwhenthen.TestState;
import com.integreety.yatspec.e2e.captor.repository.InterceptedDocumentRepository;
import com.integreety.yatspec.e2e.captor.repository.model.InterceptedCall;
import com.integreety.yatspec.e2e.teststate.TestStateCollector;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static com.integreety.yatspec.e2e.captor.repository.model.Type.*;
import static com.integreety.yatspec.e2e.teststate.mapper.destination.UserSuppliedDestinationMappings.userSuppliedDestinationMappings;
import static com.integreety.yatspec.e2e.teststate.mapper.source.UserSuppliedSourceMappings.userSuppliedSourceMappings;
import static java.util.Collections.singletonList;
import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentCaptor.forClass;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class TestStateCollectorIT {

    private final TestState testState = mock(TestState.class);
    private final InterceptedDocumentRepository interceptedDocumentRepository = mock(InterceptedDocumentRepository.class);
    private final TestStateCollector underTest = new TestStateCollector(testState, interceptedDocumentRepository);
    private final ArgumentCaptor<Object> acObject = forClass(Object.class);
    private final ArgumentCaptor<String> acString = forClass(String.class);

    private final String traceId = randomAlphanumeric(8);

    @ParameterizedTest
    @MethodSource("provideInterceptedCalls")
    public void shouldGenerateRequestInteractionNames(final InterceptedCall interceptedCall, final String expectedInteractionName) {

        given(interceptedDocumentRepository.findByTraceId(traceId)).willReturn(singletonList(interceptedCall));

        underTest.logStatesFromDatabase(traceId, userSuppliedSourceMappings(Map.of()), userSuppliedDestinationMappings(Map.of()));

        verify(testState).log(acString.capture(), acObject.capture());

        final List<String> interactionNames = acString.getAllValues();
        assertThat(interactionNames, hasSize(1));
        assertThat(interactionNames.get(0), is(expectedInteractionName));
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