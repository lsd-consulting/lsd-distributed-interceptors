package io.lsdconsulting.lsd.distributed.interceptor.captor.http;

import feign.Request;
import io.lsdconsulting.lsd.distributed.access.model.InterceptedInteraction;
import io.lsdconsulting.lsd.distributed.interceptor.captor.convert.TypeConverter;
import io.lsdconsulting.lsd.distributed.interceptor.captor.http.derive.PathDeriver;
import io.lsdconsulting.lsd.distributed.interceptor.captor.http.derive.SourceTargetDeriver;
import io.lsdconsulting.lsd.distributed.interceptor.captor.trace.TraceIdRetriever;
import io.lsdconsulting.lsd.distributed.interceptor.persistance.QueueService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpRequest;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.Map;

import static io.lsdconsulting.lsd.distributed.access.model.InteractionType.REQUEST;
import static java.util.Collections.emptyMap;

@Slf4j
@RequiredArgsConstructor
public class RequestCaptor {

    private final QueueService queueService;
    private final SourceTargetDeriver sourceTargetDeriver;
    private final PathDeriver pathDeriver;
    private final TraceIdRetriever traceIdRetriever;
    private final HttpHeaderRetriever httpHeaderRetriever;
    private final String profile;

    public InterceptedInteraction captureRequestInteraction(final Request request) {
        final var headers = httpHeaderRetriever.retrieve(request);
        final String body = TypeConverter.convert(request.body());
        final String path = pathDeriver.derivePathFrom(request.url());
        final String traceId = traceIdRetriever.getTraceId(headers);
        final String target = sourceTargetDeriver.deriveTarget(headers, path);
        final String serviceName = sourceTargetDeriver.deriveServiceName(headers);
        final InterceptedInteraction interceptedInteraction = buildInterceptedInteraction(headers, body, path, traceId, target, serviceName, request.httpMethod().name());
        queueService.enqueue(interceptedInteraction);
        return interceptedInteraction;
    }

    public InterceptedInteraction captureRequestInteraction(final HttpRequest request, final String body) {
        final var headers = httpHeaderRetriever.retrieve(request);
        final String path = pathDeriver.derivePathFrom(request);
        final String traceId = traceIdRetriever.getTraceId(headers);
        final String target = sourceTargetDeriver.deriveTarget(headers, path);
        final String serviceName = sourceTargetDeriver.deriveServiceName(headers);
        final InterceptedInteraction interceptedInteraction = buildInterceptedInteraction(headers, body, path, traceId, target, serviceName, request.getMethodValue());
        queueService.enqueue(interceptedInteraction);
        return interceptedInteraction;
    }

    private InterceptedInteraction buildInterceptedInteraction(Map<String, Collection<String>> headers, String body, String path, String traceId, String target, String serviceName, String httpMethod) {
        return InterceptedInteraction.builder()
                .traceId(traceId)
                .body(body)
                .requestHeaders(headers)
                .responseHeaders(emptyMap())
                .serviceName(serviceName)
                .target(target)
                .path(path)
                .httpMethod(httpMethod)
                .interactionType(REQUEST)
                .profile(profile)
                .createdAt(ZonedDateTime.now(ZoneId.of("UTC")))
                .build();
    }
}
