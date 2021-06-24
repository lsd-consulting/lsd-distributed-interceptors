package com.integreety.yatspec.e2e.teststate;

import com.integreety.yatspec.e2e.captor.repository.InterceptedDocumentRepository;
import com.integreety.yatspec.e2e.captor.repository.model.InterceptedInteraction;
import com.integreety.yatspec.e2e.teststate.interaction.InteractionNameGenerator;
import com.lsd.LsdContext;
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

    public void logStatesFromDatabase(final String... traceIds) {
        final Map<String, Optional<String>> traceIdToColourMap = new HashMap<>();
        Arrays.stream(traceIds).forEach(x -> traceIdToColourMap.put(x, Optional.empty()));
        logStatesFromDatabase(traceIdToColourMap);
    }

    public void logStatesFromDatabase(final Map<String, Optional<String>> traceIdToColourMap) {

        final String[] traceIds = traceIdToColourMap.keySet().toArray(new String[0]);
        final List<InterceptedInteraction> interceptedInteractions = interceptedDocumentRepository.findByTraceIds(traceIds);
        for (final Pair<String, String> interaction : interactionNameGenerator.generate(interceptedInteractions, traceIdToColourMap)) {
            lsdContext.capture(interaction.getLeft(), interaction.getRight());
        }
    }
}