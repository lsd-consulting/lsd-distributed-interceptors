package com.integreety.yatspec.e2e.teststate.mapper.source;

import org.apache.commons.lang3.tuple.Pair;

public interface SourceNameMappings {
    SourceNameMappings ALWAYS_APP = path -> "App";

    String mapFor(Pair<String, String> pair);
}
