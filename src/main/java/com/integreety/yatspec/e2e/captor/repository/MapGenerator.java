package com.integreety.yatspec.e2e.captor.repository;

import lombok.RequiredArgsConstructor;

import java.util.Collection;
import java.util.Map;

@RequiredArgsConstructor
public class MapGenerator {

    private final TraceIdRetriever traceIdRetriever;

    public Map<String, Object> generateFrom(final String body,
                                            final Map<String, Collection<String>> headers,
                                            final String interactionName, final Type type) {

        return Map.of(
                "traceId", traceIdRetriever.getTraceId(headers),
                "type", type,
                "body", body,
                "headers", headers,
                "interactionName", interactionName
        );
    }
}
