package com.integreety.yatspec.e2e.teststate.mapper.source;

import org.apache.commons.lang3.tuple.Pair;

import java.util.Map;

public interface SourceNameMappings {
    String mapFor(Pair<String, String> pair);
    Map<Pair<String, String>, String> getUnusedMappings();
}
