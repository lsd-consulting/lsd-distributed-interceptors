package com.integreety.yatspec.e2e.captor.http;

import com.integreety.yatspec.e2e.captor.http.mapper.destination.DestinationNameMappings;
import com.integreety.yatspec.e2e.captor.http.mapper.source.SourceNameMappings;
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
import static com.integreety.yatspec.e2e.captor.template.InteractionMessageTemplates.requestOf;

@Slf4j
@RequiredArgsConstructor
public class RequestCaptor extends PathDerivingCaptor {

    private final InterceptedDocumentRepository interceptedDocumentRepository;
    private final InterceptedCallFactory interceptedCallFactory;
    private final SourceNameMappings sourceNameMappings;
    private final DestinationNameMappings destinationNameMappings;

    public void captureRequestInteraction(final Request request) {
        try {
            final String body = new String(request.body());
            final String path = derivePath(request.url());
            final String source = sourceNameMappings.mapForPath(path);
            final String destination = destinationNameMappings.mapForPath(path);
            final String interactionMessage = requestOf(request.httpMethod().name(), path, source, destination);
            interceptedDocumentRepository.save(interceptedCallFactory.buildFrom(body, request.headers(), interactionMessage, REQUEST));
        } catch (final RuntimeException e) {
            log.error(e.getMessage(), e);
            throw e;
        }
    }

    public void captureRequestInteraction(final HttpRequest request, final String body) {
        final String path = request.getURI().getPath();
        final String source = sourceNameMappings.mapForPath(path);
        final String destination = destinationNameMappings.mapForPath(path);
        final String interactionMessage = requestOf(request.getMethodValue(), path, source, destination);
        final var headers = request.getHeaders().entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> (Collection<String>) e.getValue()));
        interceptedDocumentRepository.save(interceptedCallFactory.buildFrom(body, headers, interactionMessage, REQUEST));
    }
}