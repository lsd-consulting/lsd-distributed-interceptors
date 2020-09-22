package com.integreety.yatspec.e2e.captor.http;

import com.integreety.yatspec.e2e.captor.http.mapper.DestinationNameMappings;
import com.integreety.yatspec.e2e.captor.name.ServiceNameDeriver;
import com.integreety.yatspec.e2e.captor.repository.InterceptedDocumentRepository;
import com.integreety.yatspec.e2e.captor.repository.MapGenerator;
import feign.Request;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpRequest;

import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

import static com.integreety.yatspec.e2e.captor.repository.Type.REQUEST;
import static com.integreety.yatspec.e2e.captor.template.InteractionMessageTemplates.requestOf;

@Slf4j
@RequiredArgsConstructor
public class RequestCaptor extends PathDerivingCaptor {

    private final InterceptedDocumentRepository interceptedDocumentRepository;
    private final MapGenerator mapGenerator;
    private final ServiceNameDeriver serviceNameDeriver;
    private final DestinationNameMappings destinationNameMappings;

    public void captureRequestInteraction(final Request request) {
        try {
            final String body = request.requestBody().asString();
            final String path = derivePath(request.url());
            final String source = serviceNameDeriver.derive();
            final String destination = destinationNameMappings.mapForPath(path);
            final String interactionMessage = requestOf(request.httpMethod().name(), path, source, destination);
            final Map<String, Object> data = mapGenerator.generateFrom(body, request.headers(), interactionMessage, REQUEST);
            interceptedDocumentRepository.save(data);
        } catch (final RuntimeException e) {
            log.error(e.getMessage(), e);
            throw e;
        }
    }

    public void captureRequestInteraction(final HttpRequest request, final String body) {
        final String path = request.getURI().getPath();
        final String source = serviceNameDeriver.derive();
        final String destination = destinationNameMappings.mapForPath(path);
        final String interactionMessage = requestOf(request.getMethodValue(), path, source, destination);
        final var headers = request.getHeaders().entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> (Collection<String>) e.getValue()));
        final Map<String, Object> data = mapGenerator.generateFrom(body, headers, interactionMessage, REQUEST);
        interceptedDocumentRepository.save(data);
    }
}