package com.integreety.yatspec.e2e.teststate.indent;

import org.junit.jupiter.api.Test;

import java.util.Optional;

import static java.util.Optional.empty;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class XmlPrettyPrinterShould {

    @Test
    public void indentXml() {
        final Optional<String> result = XmlPrettyPrinter.indentXml("<?xml version=\"1.0\" encoding=\"UTF-8\"?><parent attribute=\"abc\"><child>value</child></parent>");
        assertTrue(result.isPresent());
        assertThat(result.get(), is("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n\n<parent attribute=\"abc\">\n  <child>value</child>\n</parent>\n"));
    }

    @Test
    public void handleEmptyString() {
        final Optional<String> result = XmlPrettyPrinter.indentXml(null);
        assertThat(result, is(empty()));
    }
}