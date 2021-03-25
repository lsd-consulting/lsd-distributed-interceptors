package com.integreety.yatspec.e2e.teststate;

import com.googlecode.yatspec.state.givenwhenthen.TestState;
import com.integreety.yatspec.e2e.captor.repository.InterceptedDocumentRepository;
import com.integreety.yatspec.e2e.captor.repository.model.InterceptedInteraction;
import com.integreety.yatspec.e2e.teststate.interaction.InteractionNameGenerator;
import org.junit.jupiter.api.Test;

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
        final InterceptedInteraction interceptedInteraction = InterceptedInteraction.builder().build();
        given(interceptedDocumentRepository.findByTraceIds(traceId)).willReturn(singletonList(interceptedInteraction));
        given(interactionNameGenerator.generate(eq(singletonList(interceptedInteraction)), any()))
                .willReturn(singletonList(of("interactionName", "body")));

        underTest.logStatesFromDatabase(traceId);

        verify(testState).log(anyString(), anyString());
    }
}