package io.lsdconsulting.lsd.distributed.interceptor.captor.http;

import feign.Request;
import io.lsdconsulting.lsd.distributed.access.model.InterceptedInteraction;
import io.lsdconsulting.lsd.distributed.access.repository.InterceptedDocumentRepository;
import io.lsdconsulting.lsd.distributed.interceptor.captor.convert.TypeConverter;
import io.lsdconsulting.lsd.distributed.interceptor.captor.header.HeaderRetriever;
import io.lsdconsulting.lsd.distributed.interceptor.captor.http.derive.PathDeriver;
import io.lsdconsulting.lsd.distributed.interceptor.captor.http.derive.SourceTargetDeriver;
import io.lsdconsulting.lsd.distributed.interceptor.captor.trace.TraceIdRetriever;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpRequest;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.Map;

import static io.lsdconsulting.lsd.distributed.access.model.Type.REQUEST;
import static java.util.Collections.emptyMap;

@Slf4j
@RequiredArgsConstructor
public class RequestCaptor {

    private final InterceptedDocumentRepository interceptedDocumentRepository;
    private final SourceTargetDeriver sourceTargetDeriver;
    private final PathDeriver pathDeriver;
    private final TraceIdRetriever traceIdRetriever;
    private final HeaderRetriever headerRetriever;
    private final String profile;

    public InterceptedInteraction captureRequestInteraction(final Request request) {
        final var headers = headerRetriever.retrieve(request);
        final String body = TypeConverter.convert(request.body());
        final String path = pathDeriver.derivePathFrom(request.url());
        final String traceId = traceIdRetriever.getTraceId(headers);
        final String target = sourceTargetDeriver.deriveTarget(headers, path);
        final String serviceName = sourceTargetDeriver.deriveServiceName(headers);
        final InterceptedInteraction interceptedInteraction = buildInterceptedInteraction(headers, body, path, traceId, target, serviceName, request.httpMethod().name());
        interceptedDocumentRepository.save(interceptedInteraction);
        return interceptedInteraction;
    }

    public InterceptedInteraction captureRequestInteraction(final HttpRequest request, final String body) {
        final var headers = headerRetriever.retrieve(request);
        final String path = pathDeriver.derivePathFrom(request);
        final String traceId = traceIdRetriever.getTraceId(headers);
        final String target = sourceTargetDeriver.deriveTarget(headers, path);
        final String serviceName = sourceTargetDeriver.deriveServiceName(headers);
        final InterceptedInteraction interceptedInteraction = buildInterceptedInteraction(headers, body, path, traceId, target, serviceName, request.getMethodValue());
        interceptedDocumentRepository.save(interceptedInteraction);
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
                .type(REQUEST)
                .profile(profile)
                .createdAt(ZonedDateTime.now(ZoneId.of("UTC")))
                .build();
    }
}
