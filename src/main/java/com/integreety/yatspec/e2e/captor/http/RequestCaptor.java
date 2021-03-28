package com.integreety.yatspec.e2e.captor.http;

import com.integreety.yatspec.e2e.captor.http.mapper.PropertyServiceNameDeriver;
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

import static com.integreety.yatspec.e2e.captor.repository.model.Type.REQUEST;

@Slf4j
@RequiredArgsConstructor
public class RequestCaptor extends PathDerivingCaptor {

    private final InterceptedDocumentRepository interceptedDocumentRepository;
    private final InterceptedInteractionFactory interceptedInteractionFactory;
    private final PropertyServiceNameDeriver propertyServiceNameDeriver;
    private final TraceIdRetriever traceIdRetriever;

    public InterceptedInteraction captureRequestInteraction(final Request request) {
        try {
            final Map<String, Collection<String>> headers = request.headers();
            final String body = request.body() != null ? new String(request.body()) : null;
            final String path = derivePath(request.url());
            final String traceId = traceIdRetriever.getTraceId(headers);
            final String target = headerExists(headers, TARGET_NAME_KEY) ? findHeader(headers, TARGET_NAME_KEY).orElse(derivePath(request.url())) : derivePath(request.url());
            final String serviceName = headerExists(headers, SOURCE_NAME_KEY) ? findHeader(headers, SOURCE_NAME_KEY).orElse(propertyServiceNameDeriver.getServiceName()) : propertyServiceNameDeriver.getServiceName();
            final InterceptedInteraction interceptedInteraction = interceptedInteractionFactory.buildFrom(body, headers, traceId, serviceName, target, path, null, request.httpMethod().name(), REQUEST);
            interceptedDocumentRepository.save(interceptedInteraction);
            return interceptedInteraction;
        } catch (final RuntimeException e) {
            log.error(e.getMessage(), e);
            throw e;
        }
    }

    public InterceptedInteraction captureRequestInteraction(final HttpRequest request, final String body) {
        final var headers = request.getHeaders().entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> (Collection<String>) e.getValue()));
        final String path = generatePath(request);
        final String serviceName = headerExists(headers, SOURCE_NAME_KEY) ? findHeader(headers, SOURCE_NAME_KEY).orElse(propertyServiceNameDeriver.getServiceName()) : propertyServiceNameDeriver.getServiceName();
        final String target = headerExists(headers, TARGET_NAME_KEY) ? findHeader(headers, TARGET_NAME_KEY).orElse(generatePath(request)) : generatePath(request);
        final String traceId = traceIdRetriever.getTraceId(headers);
        final InterceptedInteraction interceptedInteraction = interceptedInteractionFactory.buildFrom(body, headers, traceId, serviceName, target, path, null, request.getMethodValue(), REQUEST);
        interceptedDocumentRepository.save(interceptedInteraction);
        return interceptedInteraction;
    }
}
