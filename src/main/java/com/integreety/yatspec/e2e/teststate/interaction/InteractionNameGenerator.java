package com.integreety.yatspec.e2e.teststate.interaction;

import com.integreety.yatspec.e2e.captor.repository.model.InterceptedInteraction;
import com.integreety.yatspec.e2e.teststate.dto.Interaction;
import com.integreety.yatspec.e2e.teststate.mapper.destination.DestinationNameMappings;
import com.integreety.yatspec.e2e.teststate.mapper.source.SourceNameMappings;
import com.integreety.yatspec.e2e.teststate.report.ReportRenderer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static com.integreety.yatspec.e2e.teststate.indent.JsonPrettyPrinter.indentJson;
import static com.integreety.yatspec.e2e.teststate.indent.XmlPrettyPrinter.indentXml;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;

@Slf4j
@RequiredArgsConstructor
public class InteractionNameGenerator {

    public List<Pair<String, Object>> generate(final SourceNameMappings sourceNameMappings,
                                               final DestinationNameMappings destinationNameMappings,
                                               final List<InterceptedInteraction> interceptedInteractions,
                                               final ReportRenderer reportRenderer) {

        final List<Pair<String, Object>> interactions = new ArrayList<>();
        for (final InterceptedInteraction interceptedInteraction : interceptedInteractions) {
            var headers = interceptedInteraction.getHeaders();
            final String destination = deriveDestinationName(destinationNameMappings, interceptedInteraction, headers);
            final String source = deriveSourceName(sourceNameMappings, interceptedInteraction, headers);
            reportRenderer.log(interceptedInteraction.getServiceName(), interceptedInteraction.getTarget(), source, destination);
            final String interactionName = interceptedInteraction.getType().getInteractionName().apply(buildInteraction(interceptedInteraction, source, destination));
            log.info("Generated an interaction name={}", interactionName);
            final String body = indent(interceptedInteraction.getBody());
            interactions.add(Pair.of(interactionName, body));
        }
        return interactions;
    }

    private String deriveSourceName(SourceNameMappings sourceNameMappings, InterceptedInteraction interceptedInteraction, Map<String, Collection<String>> headers) {
        return isNotEmpty(headers) && headers.containsKey("Source-Name")
                ? headers.get("Source-Name").stream().findFirst().orElse("")
                : sourceNameMappings.mapFor(Pair.of(interceptedInteraction.getServiceName(), interceptedInteraction.getTarget()));
    }

    private String deriveDestinationName(DestinationNameMappings destinationNameMappings, InterceptedInteraction interceptedInteraction, Map<String, Collection<String>> headers) {
        return isNotEmpty(headers) && headers.containsKey("Target-Name")
                ? headers.get("Target-Name").stream().findFirst().orElse("")
                : destinationNameMappings.mapForPath(interceptedInteraction.getTarget());
    }

    private Interaction buildInteraction(final InterceptedInteraction interceptedInteraction, final String source, final String destination) {
        return Interaction.builder()
                .source(source)
                .destination(destination)
                .httpMethod(interceptedInteraction.getHttpMethod())
                .httpStatus(interceptedInteraction.getHttpStatus())
                .path(interceptedInteraction.getTarget())
                .build();
    }

    private String indent(final String document) {
        return indentJson(document).orElseGet(() -> indentXml(document).orElse(document));
    }
}