package io.lsdconsulting.lsd.distributed.teststate;

import com.lsd.LsdContext;
import io.lsdconsulting.lsd.distributed.captor.repository.InterceptedDocumentRepository;
import io.lsdconsulting.lsd.distributed.captor.repository.model.InterceptedInteraction;
import io.lsdconsulting.lsd.distributed.teststate.interaction.InteractionGenerator;
import org.junit.jupiter.api.Test;

import static java.util.Collections.singletonList;
import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;
import static org.apache.commons.lang3.tuple.Pair.of;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class TestStateLoggerShould {

    private final InterceptedDocumentRepository interceptedDocumentRepository = mock(InterceptedDocumentRepository.class);
    private final InteractionGenerator interactionGenerator = mock(InteractionGenerator.class);
    private final LsdContext lsdContext = mock(LsdContext.class);

    private final String traceId = randomAlphanumeric(8);

    private final TestStateLogger underTest = new TestStateLogger(interceptedDocumentRepository, interactionGenerator, lsdContext);

    @Test
    public void logInteractionName() {
        final InterceptedInteraction interceptedInteraction = InterceptedInteraction.builder().build();
        given(interceptedDocumentRepository.findByTraceIds(traceId)).willReturn(singletonList(interceptedInteraction));
        given(interactionGenerator.generate(eq(singletonList(interceptedInteraction)), any()))
                .willReturn(singletonList(of("interactionName", "body")));

        underTest.captureInteractionsFromDatabase(traceId);

        verify(lsdContext).capture("interactionName", "body");
    }
}