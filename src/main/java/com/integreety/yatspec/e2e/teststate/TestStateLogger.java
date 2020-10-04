package com.integreety.yatspec.e2e.teststate;

import com.googlecode.yatspec.state.givenwhenthen.TestState;
import com.integreety.yatspec.e2e.captor.repository.InterceptedDocumentRepository;
import com.integreety.yatspec.e2e.captor.repository.model.InterceptedCall;
import com.integreety.yatspec.e2e.teststate.interaction.InteractionNameGenerator;
import com.integreety.yatspec.e2e.teststate.mapper.destination.DestinationNameMappings;
import com.integreety.yatspec.e2e.teststate.mapper.source.SourceNameMappings;
import com.integreety.yatspec.e2e.teststate.report.ReportRenderer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
public class TestStateLogger {

    private final TestState testState;
    private final InterceptedDocumentRepository interceptedDocumentRepository;
    private final InteractionNameGenerator interactionNameGenerator;

    public void logStatesFromDatabase(final String traceId, final SourceNameMappings sourceNameMappings,
                                      final DestinationNameMappings destinationNameMappings) {

        final List<InterceptedCall> interceptedCalls = interceptedDocumentRepository.findByTraceId(traceId);
        final ReportRenderer reportRenderer = new ReportRenderer();
        for (final Pair<String, Object> interaction : interactionNameGenerator.generate(sourceNameMappings, destinationNameMappings, interceptedCalls, reportRenderer)) {
            testState.log(interaction.getLeft(), interaction.getRight());
        }
        reportRenderer.logUnusedSourceMappings(sourceNameMappings.getUnusedMappings());
        reportRenderer.logUnusedDestinationMappings(destinationNameMappings.getUnusedMappings());
        reportRenderer.printTo(System.out);
    }
}