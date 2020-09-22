package com.integreety.yatspec.e2e.captor.repository;

import com.integreety.yatspec.e2e.captor.repository.model.InterceptedCall;
import com.integreety.yatspec.e2e.captor.repository.model.Type;
import lombok.RequiredArgsConstructor;

import java.util.Collection;
import java.util.Map;

@RequiredArgsConstructor
public class MapGenerator {

    private final TraceIdRetriever traceIdRetriever;

    public InterceptedCall generateFrom(final String body,
                                        final Map<String, Collection<String>> headers,
                                        final String interactionName, final Type type) {

        return InterceptedCall.builder()
                .traceId(traceIdRetriever.getTraceId(headers))
                .type(type)
                .body(body)
                .headers(headers)
                .interactionName(interactionName)
                .build();
    }
}
