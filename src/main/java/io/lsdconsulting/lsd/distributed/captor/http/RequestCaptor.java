package io.lsdconsulting.lsd.distributed.captor.http;

import feign.Request;
import io.lsdconsulting.lsd.distributed.captor.convert.TypeConverter;
import io.lsdconsulting.lsd.distributed.captor.header.HeaderRetriever;
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

@Slf4j
@RequiredArgsConstructor
public class RequestCaptor {

    private final InterceptedDocumentRepository interceptedDocumentRepository;
    private final InterceptedInteractionFactory interceptedInteractionFactory;
    private final SourceTargetDeriver sourceTargetDeriver;
    private final PathDeriver pathDeriver;
    private final TraceIdRetriever traceIdRetriever;
    private final HeaderRetriever headerRetriever;

    public InterceptedInteraction captureRequestInteraction(final Request request) {
        final var headers = headerRetriever.retrieve(request);
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
        final var headers = headerRetriever.retrieve(request);
        final String path = pathDeriver.derivePathFrom(request);
        final String traceId = traceIdRetriever.getTraceId(headers);
        final String target = sourceTargetDeriver.deriveTarget(headers, path);
        final String serviceName = sourceTargetDeriver.deriveServiceName(headers);
        final InterceptedInteraction interceptedInteraction = interceptedInteractionFactory.buildFrom(body, headers, traceId, serviceName, target, path, null, request.getMethodValue(), Type.REQUEST);
        interceptedDocumentRepository.save(interceptedInteraction);
        return interceptedInteraction;
    }
}
