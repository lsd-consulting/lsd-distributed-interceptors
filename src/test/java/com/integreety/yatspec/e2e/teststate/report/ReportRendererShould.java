package com.integreety.yatspec.e2e.teststate.report;

import com.googlecode.totallylazy.StringPrintStream;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;

class ReportRendererShould {

    private final ReportRenderer underTest = new ReportRenderer();
    private final String serviceName = randomAlphanumeric(20);
    private final String target = randomAlphanumeric(20);
    private final String source = randomAlphanumeric(20);
    private final String destination = randomAlphanumeric(20);

    @Test
    public void alwaysPrintResolvedMappingsTable() {

        final StringPrintStream ps = new StringPrintStream();
        underTest.printTo(ps);

        assertThat(ps.toString(), containsString("Empty"));
    }

    @Test
    public void printResolvedMappingsTable() {
        underTest.log(serviceName, target, source, destination);

        final StringPrintStream ps = new StringPrintStream();
        underTest.printTo(ps);

        assertThat(ps.toString(), containsString("List of all resolved mappings"));
        assertThat(ps.toString(), containsString(serviceName));
        assertThat(ps.toString(), containsString(target));
        assertThat(ps.toString(), containsString(source));
        assertThat(ps.toString(), containsString(destination));
    }

    @Test
    public void printUnusedSourceMappingsWarning() {
        underTest.logUnusedSourceMappings(Map.of(Pair.of(serviceName, target), source));

        final StringPrintStream ps = new StringPrintStream();
        underTest.printTo(ps);

        assertThat(ps.toString(), containsString("SOURCE MAPPING WARNING"));
        assertThat(ps.toString(), containsString(serviceName));
        assertThat(ps.toString(), containsString(target));
        assertThat(ps.toString(), containsString(source));
    }

    @Test
    public void notPrintTheUnusedSourceMappingsTable() {
        final StringPrintStream ps = new StringPrintStream();
        underTest.printTo(ps);

        assertThat(ps.toString(), not(containsString("SOURCE MAPPING WARNING")));
    }

    @Test
    public void printUnusedDestinationMappingsWarning() {
        underTest.logUnusedDestinationMappings(Map.of(target, destination));

        final StringPrintStream ps = new StringPrintStream();
        underTest.printTo(ps);

        assertThat(ps.toString(), containsString("DESTINATION MAPPING WARNING"));
        assertThat(ps.toString(), containsString(target));
        assertThat(ps.toString(), containsString(destination));
    }

    @Test
    public void notPrintTheUnusedDestinationMappingsTable() {
        final StringPrintStream ps = new StringPrintStream();
        underTest.printTo(ps);

        assertThat(ps.toString(), not(containsString("DESTINATION MAPPING WARNING")));
    }

    @Test
    public void printAllTables() {
        underTest.log(serviceName, target, source, destination);
        underTest.logUnusedSourceMappings(Map.of(Pair.of(serviceName, target), source));
        underTest.logUnusedDestinationMappings(Map.of(target, destination));

        final StringPrintStream ps = new StringPrintStream();
        underTest.printTo(ps);

        assertThat(ps.toString(), containsString("List of all resolved mappings"));
        assertThat(ps.toString(), containsString("SOURCE MAPPING WARNING"));
        assertThat(ps.toString(), containsString("DESTINATION MAPPING WARNING"));
    }

}