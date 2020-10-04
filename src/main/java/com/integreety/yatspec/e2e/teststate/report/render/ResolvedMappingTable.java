package com.integreety.yatspec.e2e.teststate.report.render;

import de.vandermeer.asciitable.AT_Row;
import de.vandermeer.asciitable.AsciiTable;

import java.util.List;
import java.util.Set;

import static com.integreety.yatspec.e2e.teststate.report.ReportRenderer.LARGE_TABLE_WIDTH;
import static de.vandermeer.skb.interfaces.transformers.textformat.TextAlignment.CENTER;

public class ResolvedMappingTable {
    public String render(final Set<List<String>> reportTable) {
        final AsciiTable at = new AsciiTable();

        // Header
        at.addRule();
        final AT_Row row1 = at.addRow(null, null, null, null, "List of all resolved mappings");
        row1.setTextAlignment(CENTER);
        at.addRule();
        final AT_Row row2 = at.addRow(null, null, "Service name & path ---> Source", null, "Target ---> Destination");
        row2.setTextAlignment(CENTER);
        at.addRule();

        // Content
        for (final List<String> reportTableRow : reportTable) {
            at.addRow(reportTableRow);
        }
        at.addRule();

        // Render
        return at.render(LARGE_TABLE_WIDTH);
    }
}