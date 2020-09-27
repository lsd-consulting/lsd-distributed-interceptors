package com.integreety.yatspec.e2e.teststate;

import com.googlecode.yatspec.state.givenwhenthen.TestState;
import com.integreety.yatspec.e2e.captor.repository.InterceptedDocumentRepository;
import com.integreety.yatspec.e2e.captor.repository.model.InterceptedCall;
import com.integreety.yatspec.e2e.teststate.dto.Interaction;
import com.integreety.yatspec.e2e.teststate.mapper.destination.DestinationNameMappings;
import com.integreety.yatspec.e2e.teststate.mapper.source.SourceNameMappings;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

import static com.integreety.yatspec.e2e.teststate.indent.JsonPrettyPrinter.indentJson;
import static com.integreety.yatspec.e2e.teststate.indent.XmlPrettyPrinter.indentXml;

@Slf4j
@RequiredArgsConstructor
public class TestStateCollector {

    private final TestState testState;
    private final InterceptedDocumentRepository interceptedDocumentRepository;

    public void logStatesFromDatabase(final String traceId, final SourceNameMappings sourceNameMappings,
                                      final DestinationNameMappings destinationNameMappings) {

        final List<InterceptedCall> data = interceptedDocumentRepository.findByTraceId(traceId);
        for (final InterceptedCall interceptedCall : data) {
            final String destination = destinationNameMappings.mapForPath(interceptedCall.getTarget());
            final String source = sourceNameMappings.mapFor(Pair.of(interceptedCall.getServiceName(), interceptedCall.getTarget()));
            log.info("Resolved service name:{}, to source:{}, and target:{}, to destination:{}", interceptedCall.getServiceName(), source, interceptedCall.getTarget(), destination);
            final String interactionName = interceptedCall.getType().getInteractionName().apply(buildInteraction(interceptedCall, destination, source));
            log.info("Generated an interaction name={}", interactionName);
            testState.log(interactionName, indent(interceptedCall.getBody()));
        }
    }

    private Interaction buildInteraction(final InterceptedCall interceptedCall, final String destination, final String source) {
        return Interaction.builder()
                        .source(source)
                        .destination(destination)
                        .httpMethod(interceptedCall.getHttpMethod())
                        .httpStatus(interceptedCall.getHttpStatus())
                        .path(interceptedCall.getTarget())
                        .build();
    }

    private String indent(final String document) {
        return indentJson(document).orElseGet(() -> indentXml(document).orElse(null));
    }
}