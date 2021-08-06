package io.lsdconsulting.lsd.distributed.captor.repository.model;

import lombok.RequiredArgsConstructor;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.Map;

import static java.util.Collections.emptyMap;

@RequiredArgsConstructor
public class InterceptedInteractionFactory {

    private final String profile;

    public InterceptedInteraction buildFrom(final String body, final String traceId,
                                            final Map<String, Collection<String>> headers, final String serviceName,
                                            final String target, final String path, final Type type) {

        return buildFrom(body, headers, traceId, serviceName, target, path, null, null, type);
    }

    public InterceptedInteraction buildFrom(final String body, final Map<String, Collection<String>> requestHeaders, final String traceId,
                                            final String serviceName, final String target, final String path, final String httpStatus,
                                            final String httpMethod, final Type type) {

        return buildFrom(body, requestHeaders, emptyMap(), traceId, serviceName, target, path, httpStatus, null, httpMethod, type);
    }

    public InterceptedInteraction buildFrom(final String body, final Map<String, Collection<String>> requestHeaders, final Map<String, Collection<String>> responseHeaders, final String traceId,
                                            final String serviceName, final String target, final String path, final String httpStatus, final Long elapsedTime,
                                            final String httpMethod, final Type type) {

        return InterceptedInteraction.builder()
                .traceId(traceId)
                .body(body)
                .requestHeaders(requestHeaders)
                .responseHeaders(responseHeaders)
                .serviceName(serviceName)
                .target(target)
                .path(path)
                .httpStatus(httpStatus)
                .httpMethod(httpMethod)
                .type(type)
                .profile(profile)
                .elapsedTime(elapsedTime)
                .createdAt(ZonedDateTime.now(ZoneId.of("UTC")))
                .build();
    }
}
