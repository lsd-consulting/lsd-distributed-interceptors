package com.integreety.yatspec.e2e.captor.http;

import com.integreety.yatspec.e2e.captor.http.mapper.PropertyServiceNameDeriver;
import com.integreety.yatspec.e2e.captor.repository.InterceptedDocumentRepository;
import com.integreety.yatspec.e2e.captor.repository.model.InterceptedCallFactory;
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

    public void captureRequestInteraction(final Request request) {
        try {
            final String body = request.body() != null ? new String(request.body()) : null;
            final String path = derivePath(request.url());
            final String serviceName = propertyServiceNameDeriver.getServiceName();
            interceptedDocumentRepository.save(interceptedCallFactory.buildFrom(body, request.headers(), serviceName, path, null, request.httpMethod().name(), REQUEST));
        } catch (final RuntimeException e) {
            log.error(e.getMessage(), e);
            throw e;
        }
    }

    public void captureRequestInteraction(final HttpRequest request, final String body) {
        final String path = request.getURI().getPath();
        final String serviceName = propertyServiceNameDeriver.getServiceName();
        final var headers = request.getHeaders().entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> (Collection<String>) e.getValue()));
        interceptedDocumentRepository.save(interceptedCallFactory.buildFrom(body, headers, serviceName, path, null, request.getMethodValue(), REQUEST));
    }
}