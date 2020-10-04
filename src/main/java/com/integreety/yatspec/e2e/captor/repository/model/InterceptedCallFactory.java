package com.integreety.yatspec.e2e.captor.repository.model;

import com.integreety.yatspec.e2e.captor.trace.TraceIdRetriever;
import lombok.RequiredArgsConstructor;

import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.Map;

@RequiredArgsConstructor
public class InterceptedCallFactory {

    private final TraceIdRetriever traceIdRetriever; // TODO Should this be part of this class?
    private final String profile;

    public InterceptedCall buildFrom(final String body,
                                     final Map<String, Collection<String>> headers, final String serviceName,
                                     final String target, final Type type) {

        return buildFrom(body, headers, serviceName, target, null, null, type);
    }

    public InterceptedCall buildFrom(final String body, final Map<String, Collection<String>> headers,
                                     final String serviceName, final String target, final String httpStatus,
                                     final String httpMethod, final Type type) {

        return InterceptedCall.builder()
                .traceId(traceIdRetriever.getTraceId(headers))
                .body(body)
                .headers(headers)
                .serviceName(serviceName)
                .target(target)
                .httpStatus(httpStatus)
                .httpMethod(httpMethod)
                .type(type)
                .profile(profile)
                .createdAt(ZonedDateTime.now())
                .build();
    }
}
