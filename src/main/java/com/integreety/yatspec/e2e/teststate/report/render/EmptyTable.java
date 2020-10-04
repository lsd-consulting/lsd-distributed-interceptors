package com.integreety.yatspec.e2e.teststate.report.render;

import de.vandermeer.asciitable.AT_Row;
import de.vandermeer.asciitable.AsciiTable;

import static de.vandermeer.skb.interfaces.transformers.textformat.TextAlignment.CENTER;
import static java.util.Arrays.asList;

public class EmptyTable {
    public String render(final String... values) {
        final AsciiTable at = new AsciiTable();

        // Header
        at.addRule();
        final AT_Row headerRow = at.addRow(asList(values));
        headerRow.setTextAlignment(CENTER);
        at.addRule();

        // Content
        final AT_Row emptyRow = at.addRow(null, "Empty");
        emptyRow.setTextAlignment(CENTER);
        at.addRule();

        // Render
        return at.render();
    }
}