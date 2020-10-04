package com.integreety.yatspec.e2e.teststate;

import com.googlecode.yatspec.state.givenwhenthen.TestState;
import com.integreety.yatspec.e2e.captor.repository.InterceptedDocumentRepository;
import com.integreety.yatspec.e2e.captor.repository.model.InterceptedCall;
import com.integreety.yatspec.e2e.teststate.interaction.InteractionNameGenerator;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static com.integreety.yatspec.e2e.teststate.mapper.destination.UserSuppliedDestinationMappings.userSuppliedDestinationMappings;
import static com.integreety.yatspec.e2e.teststate.mapper.source.UserSuppliedSourceMappings.userSuppliedSourceMappings;
import static java.util.Collections.singletonList;
import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;
import static org.apache.commons.lang3.tuple.Pair.of;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class TestStateLoggerShould {

    private final TestState testState = mock(TestState.class);
    private final InterceptedDocumentRepository interceptedDocumentRepository = mock(InterceptedDocumentRepository.class);
    private final InteractionNameGenerator interactionNameGenerator = mock(InteractionNameGenerator.class);

    private final TestStateLogger underTest = new TestStateLogger(testState, interceptedDocumentRepository, interactionNameGenerator);

    private final String traceId = randomAlphanumeric(8);

    @Test
    public void logInteractionName() {
        final InterceptedCall interceptedCall = InterceptedCall.builder().build();
        given(interceptedDocumentRepository.findByTraceId(traceId)).willReturn(singletonList(interceptedCall));
        given(interactionNameGenerator.generate(any(), any(), eq(singletonList(interceptedCall)), any()))
                .willReturn(singletonList(of("interactionName", "body")));

        underTest.logStatesFromDatabase(traceId, userSuppliedSourceMappings(Map.of()), userSuppliedDestinationMappings(Map.of()));

        verify(testState).log(anyString(), anyString());
    }
}