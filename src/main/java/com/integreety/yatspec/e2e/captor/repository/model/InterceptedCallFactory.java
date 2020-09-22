package com.integreety.yatspec.e2e.captor.repository.model;

import com.integreety.yatspec.e2e.captor.trace.TraceIdRetriever;
import lombok.RequiredArgsConstructor;

import java.util.Collection;
import java.util.Map;

@RequiredArgsConstructor
public class InterceptedCallFactory {

    private final TraceIdRetriever traceIdRetriever; // TODO Should this be part of this class?

    public InterceptedCall buildFrom(final String body,
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
