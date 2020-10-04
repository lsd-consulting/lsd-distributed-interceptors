package com.integreety.yatspec.e2e.teststate.report.render;

import de.vandermeer.asciitable.AT_Row;
import de.vandermeer.asciitable.AsciiTable;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Map;

import static com.integreety.yatspec.e2e.teststate.report.ReportRenderer.LARGE_TABLE_WIDTH;
import static de.vandermeer.skb.interfaces.transformers.textformat.TextAlignment.CENTER;

public class UnusedSourceMappingTable {
    public String render(final Map<Pair<String, String>, String> unusedSourceMappings) {
        final AsciiTable at = new AsciiTable();

        // Header
        at.addRule();
        final AT_Row row1 = at.addRow(null, null, "SOURCE MAPPING WARNING!!! Some of the user supplied source mappings were not used.");
        row1.setTextAlignment(CENTER);
        at.addRule();
        final AT_Row row2 = at.addRow(null, "Service name & path", "Source");
        row2.setTextAlignment(CENTER);
        at.addRule();

        // Content
        for (final Pair<String, String> key : unusedSourceMappings.keySet()) {
            at.addRow(key.getLeft(), key.getRight(), unusedSourceMappings.get(key));
        }
        at.addRule();

        // Render
        return at.render(LARGE_TABLE_WIDTH);
    }
}