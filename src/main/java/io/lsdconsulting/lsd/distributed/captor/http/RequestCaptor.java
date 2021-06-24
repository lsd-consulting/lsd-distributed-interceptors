package io.lsdconsulting.lsd.distributed.captor.http;

import feign.Request;
import io.lsdconsulting.lsd.distributed.captor.convert.TypeConverter;
import io.lsdconsulting.lsd.distributed.captor.http.derive.PathDeriver;
import io.lsdconsulting.lsd.distributed.captor.http.derive.SourceTargetDeriver;
import io.lsdconsulting.lsd.distributed.captor.repository.InterceptedDocumentRepository;
import io.lsdconsulting.lsd.distributed.captor.repository.model.InterceptedInteraction;
import io.lsdconsulting.lsd.distributed.captor.repository.model.InterceptedInteractionFactory;
import io.lsdconsulting.lsd.distributed.captor.repository.model.Type;
import io.lsdconsulting.lsd.distributed.captor.trace.TraceIdRetriever;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpRequest;

import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

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
        final String body = TypeConverter.convert(request.body());
        final String path = pathDeriver.derivePathFrom(request.url());
        final String traceId = traceIdRetriever.getTraceId(headers);
        final String target = sourceTargetDeriver.deriveTarget(headers, path);
        final String serviceName = sourceTargetDeriver.deriveServiceName(headers);
        final InterceptedInteraction interceptedInteraction = interceptedInteractionFactory.buildFrom(body, headers, traceId, serviceName, target, path, null, request.httpMethod().name(), Type.REQUEST);
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
        final InterceptedInteraction interceptedInteraction = interceptedInteractionFactory.buildFrom(body, headers, traceId, serviceName, target, path, null, request.getMethodValue(), Type.REQUEST);
        interceptedDocumentRepository.save(interceptedInteraction);
        return interceptedInteraction;
    }

}
