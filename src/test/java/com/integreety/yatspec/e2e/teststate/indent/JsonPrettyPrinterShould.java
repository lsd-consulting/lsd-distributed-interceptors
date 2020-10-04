package com.integreety.yatspec.e2e.teststate.indent;

import org.junit.jupiter.api.Test;

import java.util.Optional;

import static java.util.Optional.empty;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class JsonPrettyPrinterShould {

    @Test
    public void indentJson() {
        final Optional<String> result = JsonPrettyPrinter.indentJson("{\"key\":\"value\"}");
        assertTrue(result.isPresent());
        assertThat(result.get(), is("{\n  \"key\": \"value\"\n}"));
    }

    @Test
    public void handleEmptyString() {
        final Optional<String> result = JsonPrettyPrinter.indentJson(null);
        assertThat(result, is(empty()));
    }

    @Test
    public void handleNonJson() {
        final Optional<String> result = JsonPrettyPrinter.indentJson("< >");
        assertThat(result, is(empty()));
    }
}