package io.lsdconsulting.lsd.distributed.diagram;

import com.lsd.LsdContext;
import io.lsdconsulting.lsd.distributed.captor.repository.InterceptedDocumentRepository;
import io.lsdconsulting.lsd.distributed.diagram.interaction.InteractionGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
public class LsdLogger {
    private final InterceptedDocumentRepository interceptedDocumentRepository;
    private final InteractionGenerator interactionGenerator;
    private final LsdContext lsdContext;

    public void captureInteractionsFromDatabase(final String... traceIds) {
        Map<String, Optional<String>> traceIdToColourMap = new HashMap<>();
        Arrays.stream(traceIds).forEach(x -> traceIdToColourMap.put(x, Optional.empty()));
        captureInteractionsFromDatabase(traceIdToColourMap);
    }

    public void captureInteractionsFromDatabase(final Map<String, Optional<String>> traceIdToColourMap) {
        var traceIds = traceIdToColourMap.keySet().toArray(new String[0]);
        var interceptedInteractions = interceptedDocumentRepository.findByTraceIds(traceIds);
        for (var interaction : interactionGenerator.generate(interceptedInteractions, traceIdToColourMap)) {
            lsdContext.capture(interaction.getLeft(), interaction.getRight());
        }
    }
}
