package com.integreety.yatspec.e2e.captor.http;

import com.integreety.yatspec.e2e.captor.http.derive.PathDeriver;
import com.integreety.yatspec.e2e.captor.http.derive.SourceTargetDeriver;
import com.integreety.yatspec.e2e.captor.repository.InterceptedDocumentRepository;
import com.integreety.yatspec.e2e.captor.repository.model.InterceptedInteraction;
import com.integreety.yatspec.e2e.captor.repository.model.InterceptedInteractionFactory;
import com.integreety.yatspec.e2e.captor.trace.TraceIdRetriever;
import feign.Request;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpRequest;

import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

import static com.integreety.yatspec.e2e.captor.convert.TypeConverter.convert;
import static com.integreety.yatspec.e2e.captor.repository.model.Type.REQUEST;

@Slf4j
@RequiredArgsConstructor
public class RequestCaptor {

    private final InterceptedDocumentRepository interceptedDocumentRepository;
    private final InterceptedInteractionFactory interceptedInteractionFactory;
    private final SourceTargetDeriver sourceTargetDeriver;
    private final PathDeriver pathDeriver;
    private final TraceIdRetriever traceIdRetriever;

    public InterceptedInteraction captureRequestInteraction(final Request request) {
        final Map<String, Collection<String>> headers = request.headers();
        final String body = convert(request.body());
        final String path = pathDeriver.derivePathFrom(request.url());
        final String traceId = traceIdRetriever.getTraceId(headers);
        final String target = sourceTargetDeriver.deriveTarget(headers, path);
        final String serviceName = sourceTargetDeriver.deriveServiceName(headers);
        final InterceptedInteraction interceptedInteraction = interceptedInteractionFactory.buildFrom(body, headers, traceId, serviceName, target, path, null, request.httpMethod().name(), REQUEST);
        interceptedDocumentRepository.save(interceptedInteraction);
        return interceptedInteraction;
    }

    public InterceptedInteraction captureRequestInteraction(final HttpRequest request, final String body) {
        final var headers = request.getHeaders().entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> (Collection<String>) e.getValue()));
        final String path = pathDeriver.derivePathFrom(request);
        final String serviceName = sourceTargetDeriver.deriveServiceName(headers);
        final String target = sourceTargetDeriver.deriveTarget(headers, path);
        final String traceId = traceIdRetriever.getTraceId(headers);
        final InterceptedInteraction interceptedInteraction = interceptedInteractionFactory.buildFrom(body, headers, traceId, serviceName, target, path, null, request.getMethodValue(), REQUEST);
        interceptedDocumentRepository.save(interceptedInteraction);
        return interceptedInteraction;
    }

}
