package com.integreety.yatspec.e2e.teststate.report;

import com.integreety.yatspec.e2e.teststate.report.render.EmptyTable;
import com.integreety.yatspec.e2e.teststate.report.render.ResolvedMappingTable;
import com.integreety.yatspec.e2e.teststate.report.render.UnusedDestinationMappingTable;
import com.integreety.yatspec.e2e.teststate.report.render.UnusedSourceMappingTable;
import org.apache.commons.lang3.tuple.Pair;

import java.io.PrintStream;
import java.util.*;

public class ReportRenderer {

    public static final int LARGE_TABLE_WIDTH = 200;
    public static final int SMALL_TABLE_WIDTH = 150;

    private final EmptyTable emptyTable= new EmptyTable();
    private final ResolvedMappingTable resolvedMappingTable = new ResolvedMappingTable();
    private final UnusedDestinationMappingTable unusedDestinationMappingTable = new UnusedDestinationMappingTable();
    private final UnusedSourceMappingTable unusedSourceMappingTable = new UnusedSourceMappingTable();

    private final Set<List<String>> reportTable = new LinkedHashSet<>();
    private final Map<String, String> unusedDestinationMappings = new LinkedHashMap<>();
    private final Map<Pair<String, String>, String> unusedSourceMappings = new LinkedHashMap<>();

    public void log(final String serviceName, final String target, final String source, final String destination) {
            reportTable.add(List.of(serviceName, target, source, target, destination));
    }

    public void logUnusedSourceMappings(final Map<Pair<String, String>, String> unusedSourceMappings) {
        this.unusedSourceMappings.putAll(unusedSourceMappings);
    }

    public void logUnusedDestinationMappings(final Map<String, String> unusedDestinationMappings) {
        this.unusedDestinationMappings.putAll(unusedDestinationMappings);
    }

    public void printTo(final PrintStream out) {
        final String table;
        if (reportTable.size() == 0) {
            table = emptyTable.render("Service name & path ---> Source", "Target ---> Destination");
        } else {
            table = resolvedMappingTable.render(reportTable);
        }
        out.println(table);

        if (unusedSourceMappings.size() > 0) {
            out.println(unusedSourceMappingTable.render(unusedSourceMappings));
        }

        if (unusedDestinationMappings.size() > 0) {
            out.println(unusedDestinationMappingTable.render(unusedDestinationMappings));
        }
    }
}