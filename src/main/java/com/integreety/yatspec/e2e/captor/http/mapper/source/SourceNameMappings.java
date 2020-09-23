package com.integreety.yatspec.e2e.captor.http.mapper.source;

public interface SourceNameMappings {
    SourceNameMappings ALWAYS_APP = path -> "App";

    String mapForPath(String path);
}
