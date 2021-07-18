package io.lsdconsulting.lsd.distributed.teststate;

import com.lsd.LsdContext;
import io.lsdconsulting.lsd.distributed.captor.repository.InterceptedDocumentRepository;
import io.lsdconsulting.lsd.distributed.teststate.interaction.InteractionNameGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
public class TestStateLogger {
    private final InterceptedDocumentRepository interceptedDocumentRepository;
    private final InteractionNameGenerator interactionNameGenerator;
    private final LsdContext lsdContext;

    public void captureInteractionsFromDatabase(final String... traceIds) {
        Map<String, Optional<String>> traceIdToColourMap = new HashMap<>();
        Arrays.stream(traceIds).forEach(x -> traceIdToColourMap.put(x, Optional.empty()));
        captureInteractionsFromDatabase(traceIdToColourMap);
    }

    public void captureInteractionsFromDatabase(final Map<String, Optional<String>> traceIdToColourMap) {
        var traceIds = traceIdToColourMap.keySet().toArray(new String[0]);
        var interceptedInteractions = interceptedDocumentRepository.findByTraceIds(traceIds);
        for (var interaction : interactionNameGenerator.generate(interceptedInteractions, traceIdToColourMap)) {
            lsdContext.capture(interaction.getLeft(), interaction.getRight());
        }
    }
}