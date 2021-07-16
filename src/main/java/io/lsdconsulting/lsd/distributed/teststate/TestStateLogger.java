package io.lsdconsulting.lsd.distributed.teststate;

import com.lsd.LsdContext;
import io.lsdconsulting.lsd.distributed.captor.repository.InterceptedDocumentRepository;
import io.lsdconsulting.lsd.distributed.captor.repository.model.InterceptedInteraction;
import io.lsdconsulting.lsd.distributed.teststate.interaction.InteractionNameGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;

import java.util.*;

@Slf4j
@RequiredArgsConstructor
public class TestStateLogger {

    private final InterceptedDocumentRepository interceptedDocumentRepository;
    private final InteractionNameGenerator interactionNameGenerator;
    private final LsdContext lsdContext;

    public void captureInteractionsFromDatabase(final String... traceIds) {
        final Map<String, Optional<String>> traceIdToColourMap = new HashMap<>();
        Arrays.stream(traceIds).forEach(x -> traceIdToColourMap.put(x, Optional.empty()));
        captureInteractionsFromDatabase(traceIdToColourMap);
    }

    public void captureInteractionsFromDatabase(final Map<String, Optional<String>> traceIdToColourMap) {

        final String[] traceIds = traceIdToColourMap.keySet().toArray(new String[0]);
        final List<InterceptedInteraction> interceptedInteractions = interceptedDocumentRepository.findByTraceIds(traceIds);
        for (final Pair<String, String> interaction : interactionNameGenerator.generate(interceptedInteractions, traceIdToColourMap)) {
            lsdContext.capture(interaction.getLeft(), interaction.getRight());
        }
        lsdContext.completeReport("Some title");
    }
}