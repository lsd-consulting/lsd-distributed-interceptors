package com.integreety.yatspec.e2e.captor.http;

import com.integreety.yatspec.e2e.captor.http.mapper.PropertyServiceNameDeriver;
import com.integreety.yatspec.e2e.captor.repository.InterceptedDocumentRepository;
import com.integreety.yatspec.e2e.captor.repository.model.InterceptedCall;
import com.integreety.yatspec.e2e.captor.repository.model.InterceptedCallFactory;
import com.integreety.yatspec.e2e.captor.trace.TraceIdRetriever;
import feign.Request;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpRequest;

import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

import static com.integreety.yatspec.e2e.captor.repository.model.Type.REQUEST;

@Slf4j
@RequiredArgsConstructor
public class RequestCaptor extends PathDerivingCaptor {

    private final InterceptedDocumentRepository interceptedDocumentRepository;
    private final InterceptedCallFactory interceptedCallFactory;
    private final PropertyServiceNameDeriver propertyServiceNameDeriver;
    private final TraceIdRetriever traceIdRetriever;

    public InterceptedCall captureRequestInteraction(final Request request) {
        try {
            final String body = request.body() != null ? new String(request.body()) : null;
            final String path = derivePath(request.url());
            final String traceId = traceIdRetriever.getTraceId(request.headers());
            final String serviceName = propertyServiceNameDeriver.getServiceName();
            final InterceptedCall interceptedCall = interceptedCallFactory.buildFrom(body, request.headers(), traceId, serviceName, path, null, request.httpMethod().name(), REQUEST);
            interceptedDocumentRepository.save(interceptedCall);
            return interceptedCall;
        } catch (final RuntimeException e) {
            log.error(e.getMessage(), e);
            throw e;
        }
    }

    public InterceptedCall captureRequestInteraction(final HttpRequest request, final String body) {
        final String path = request.getURI().getPath();
        final String serviceName = propertyServiceNameDeriver.getServiceName();
        final var headers = request.getHeaders().entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> (Collection<String>) e.getValue()));
        final String traceId = traceIdRetriever.getTraceId(headers);
        final InterceptedCall interceptedCall = interceptedCallFactory.buildFrom(body, headers, traceId, serviceName, path, null, request.getMethodValue(), REQUEST);
        interceptedDocumentRepository.save(interceptedCall);
        return interceptedCall;
    }
}