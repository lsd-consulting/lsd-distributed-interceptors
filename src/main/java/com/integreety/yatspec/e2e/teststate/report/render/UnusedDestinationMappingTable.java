package com.integreety.yatspec.e2e.teststate.report.render;

import de.vandermeer.asciitable.AT_Row;
import de.vandermeer.asciitable.AsciiTable;

import java.util.Map;

import static com.integreety.yatspec.e2e.teststate.report.ReportRenderer.SMALL_TABLE_WIDTH;
import static de.vandermeer.skb.interfaces.transformers.textformat.TextAlignment.CENTER;

public class UnusedDestinationMappingTable {
    public String render(final Map<String, String> unusedDestinationMappings) {
        final AsciiTable at = new AsciiTable();

        // Header
        at.addRule();
        final AT_Row row1 = at.addRow(null, "DESTINATION MAPPING WARNING!!! Some of the user supplied destination mappings were not used.");
        row1.setTextAlignment(CENTER);
        at.addRule();
        final AT_Row row2 = at.addRow("Target", "Destination");
        row2.setTextAlignment(CENTER);
        at.addRule();

        // Content
        for (final String key : unusedDestinationMappings.keySet()) {
            at.addRow(key, unusedDestinationMappings.get(key));
        }
        at.addRule();

        // Render
        return at.render(SMALL_TABLE_WIDTH);
    }
}