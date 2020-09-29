package com.integreety.yatspec.e2e.teststate.interaction;

import com.integreety.yatspec.e2e.captor.repository.model.InterceptedCall;
import com.integreety.yatspec.e2e.teststate.dto.Interaction;
import com.integreety.yatspec.e2e.teststate.mapper.destination.DestinationNameMappings;
import com.integreety.yatspec.e2e.teststate.mapper.source.SourceNameMappings;
import de.vandermeer.asciitable.AT_Row;
import de.vandermeer.asciitable.AsciiTable;
import de.vandermeer.skb.interfaces.transformers.textformat.TextAlignment;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.integreety.yatspec.e2e.teststate.indent.JsonPrettyPrinter.indentJson;
import static com.integreety.yatspec.e2e.teststate.indent.XmlPrettyPrinter.indentXml;

@Slf4j
@RequiredArgsConstructor
public class InteractionNameGenerator {

    public List<Pair<String, Object>> generate(final SourceNameMappings sourceNameMappings, final DestinationNameMappings destinationNameMappings, final List<InterceptedCall> data) {
        final Set<List<String>> reportTable = new HashSet<>();
        final List<Pair<String, Object>> interactions = new ArrayList<>();
        for (final InterceptedCall interceptedCall : data) {
            final String destination = destinationNameMappings.mapForPath(interceptedCall.getTarget());
            final String source = sourceNameMappings.mapFor(Pair.of(interceptedCall.getServiceName(), interceptedCall.getTarget()));
            reportTable.add(List.of(interceptedCall.getServiceName(), interceptedCall.getTarget(), source, interceptedCall.getTarget(), destination));
            final String interactionName = interceptedCall.getType().getInteractionName().apply(buildInteraction(interceptedCall, destination, source));
            log.info("Generated an interaction name={}", interactionName);
            final String body = indent(interceptedCall.getBody());
            interactions.add(Pair.of(interactionName, body));
        }
        logReport(reportTable);
        return interactions;
    }

    private void logReport(final Set<List<String>> reportTable) {
        final AsciiTable at = new AsciiTable();
        at.addRule();
        final AT_Row row = at.addRow(null, null, "Service name & path ---> Source", null, "Target ---> Destination");
        row.setTextAlignment(TextAlignment.CENTER);
        at.addRule();
        for(final List<String> reportTableRow: reportTable) {
            at.addRow(reportTableRow);
        }
        at.addRule();

        at.getContext().setWidth(200);
        System.out.println(at.render());
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
        return indentJson(document).orElseGet(() -> indentXml(document).orElse(document));
    }
}