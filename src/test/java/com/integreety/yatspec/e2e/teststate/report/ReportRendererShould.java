package com.integreety.yatspec.e2e.teststate.report;


import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.io.PrintStream;
import java.util.Map;

import static java.lang.String.join;
import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class ReportRendererShould {

    private static final String DELIMITER = "\\n";
    
    private final ReportRenderer underTest = new ReportRenderer();
    private final String serviceName = randomAlphanumeric(20);
    private final String target = randomAlphanumeric(20);
    private final String source = randomAlphanumeric(20);
    private final String destination = randomAlphanumeric(20);

    private final PrintStream printer = mock(PrintStream.class);
    private final ArgumentCaptor<String> printedLines = ArgumentCaptor.forClass(String.class);

    @Test
    public void alwaysPrintResolvedMappingsTable() {
        underTest.printTo(printer);

        verify(printer).println(printedLines.capture());

        assertThat(join(DELIMITER, printedLines.getAllValues())).contains("Empty");
    }

    @Test
    public void printResolvedMappingsTable() {
        underTest.log(serviceName, target, source, destination);
        underTest.printTo(printer);

        verify(printer).println(printedLines.capture());

        assertThat(join(DELIMITER, printedLines.getAllValues()))
                .contains("List of all resolved mappings")
                .contains(serviceName)
                .contains(target)
                .contains(source)
                .contains(destination);
    }

    @Test
    public void printUnusedSourceMappingsWarning() {
        underTest.logUnusedSourceMappings(Map.of(Pair.of(serviceName, target), source));
        underTest.printTo(printer);

        verify(printer, times(2)).println(printedLines.capture());

        assertThat(join(DELIMITER, printedLines.getAllValues()))
                .contains("SOURCE MAPPING WARNING")
                .contains(serviceName)
                .contains(target)
                .contains(source);
    }

    @Test
    public void notPrintTheUnusedSourceMappingsTable() {
        underTest.printTo(printer);

        verify(printer).println(printedLines.capture());

        assertThat(join(DELIMITER, printedLines.getAllValues()))
                .doesNotContain("SOURCE MAPPING WARNING");
    }

    @Test
    public void printUnusedDestinationMappingsWarning() {
        underTest.logUnusedDestinationMappings(Map.of(target, destination));
        underTest.printTo(printer);

        verify(printer, times(2)).println(printedLines.capture());

        assertThat(join(DELIMITER, printedLines.getAllValues()))
                .contains("DESTINATION MAPPING WARNING")
                .contains(target)
                .contains(destination);
    }

    @Test
    public void notPrintTheUnusedDestinationMappingsTable() {
        underTest.printTo(printer);

        verify(printer).println(printedLines.capture());

        assertThat(join(DELIMITER, printedLines.getAllValues()))
                .doesNotContain("DESTINATION MAPPING WARNING");
    }

    @Test
    public void printAllTables() {
        underTest.log(serviceName, target, source, destination);
        underTest.logUnusedSourceMappings(Map.of(Pair.of(serviceName, target), source));
        underTest.logUnusedDestinationMappings(Map.of(target, destination));
        underTest.printTo(printer);

        verify(printer, times(3)).println(printedLines.capture());

        assertThat(join(DELIMITER, printedLines.getAllValues()))
                .contains("List of all resolved mappings")
                .contains("SOURCE MAPPING WARNING")
                .contains("DESTINATION MAPPING WARNING");
    }
}
