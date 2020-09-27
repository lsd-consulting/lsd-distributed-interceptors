package com.integreety.yatspec.e2e.teststate;

import com.googlecode.yatspec.state.givenwhenthen.TestState;
import com.integreety.yatspec.e2e.captor.repository.InterceptedDocumentRepository;
import com.integreety.yatspec.e2e.captor.repository.model.InterceptedCall;
import com.integreety.yatspec.e2e.teststate.mapper.destination.DestinationNameMappings;
import com.integreety.yatspec.e2e.teststate.mapper.source.SourceNameMappings;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

import static com.integreety.yatspec.e2e.teststate.indent.JsonPrettyPrinter.indentJson;
import static com.integreety.yatspec.e2e.teststate.indent.XmlPrettyPrinter.indentXml;
import static com.integreety.yatspec.e2e.teststate.template.InteractionMessageTemplates.*;

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
            final String interactionName = getInteractionName(interceptedCall, source, destination);
            log.info("Generated an interaction name={}", interactionName);
            testState.log(interactionName, indent(interceptedCall.getBody()));
        }
    }

    private String getInteractionName(final InterceptedCall interceptedCall, final String source, final String destination) {
        String interactionName = null;
        switch (interceptedCall.getType()) {
            case REQUEST:
                interactionName = requestOf(interceptedCall.getHttpMethod(), interceptedCall.getTarget(), source, destination);
                break;
            case RESPONSE:
                interactionName = responseOf(interceptedCall.getHttpStatus(), destination, source);
                break;
            case PUBLISH:
                interactionName = publishOf(source, interceptedCall.getTarget());
                break;
            case CONSUME:
                // This is just to make it less confusing
                final String consumeDestination = source;
                final String consumeSource = interceptedCall.getTarget();
                interactionName = consumeOf(consumeSource, consumeDestination);
                break;
        }
        return interactionName;
    }

    private String indent(final String document) {
        return indentJson(document).orElse(indentXml(document).orElse(null));
    }
}