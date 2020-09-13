package com.yatspec.e2e.captor.http;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yatspec.e2e.captor.http.mapper.DestinationNameMappings;
import com.yatspec.e2e.captor.name.ServiceNameDeriver;
import com.yatspec.e2e.captor.repository.InterceptedDocumentRepository;
import com.yatspec.e2e.captor.repository.MapGenerator;
import feign.Request;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.springframework.http.HttpRequest;

import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

import static com.fasterxml.jackson.databind.SerializationFeature.INDENT_OUTPUT;
import static com.yatspec.e2e.captor.repository.Type.REQUEST;
import static com.yatspec.e2e.captor.template.InteractionMessageTemplates.requestOf;

@Slf4j
@RequiredArgsConstructor
public class RequestCaptor extends PathDerivingCaptor {

    private final ObjectMapper objectMapper = new ObjectMapperCreator().getObjectMapper().enable(INDENT_OUTPUT);

    private final InterceptedDocumentRepository interceptedDocumentRepository;
    private final MapGenerator mapGenerator;
    private final ServiceNameDeriver serviceNameDeriver;
    private final DestinationNameMappings destinationNameMappings;

    @SneakyThrows
    public void captureRequestInteraction(final Request request) {
        try {
            final String body = request.requestBody().asString();
            final String path = derivePath(request.url());
            final String source = serviceNameDeriver.derive();
            final String destination = destinationNameMappings.mapForPath(path);
            final String interactionMessage = requestOf(request.httpMethod().name(), path, source, destination);
            final Map<String, Object> map = mapGenerator.generateFrom(body, request.headers(), interactionMessage, REQUEST);
            final Document document = Document.parse(objectMapper.writeValueAsString(map));
            interceptedDocumentRepository.save(document);
        } catch (final RuntimeException e) {
            log.error(e.getMessage(), e);
            throw e;
        }
    }

    public void captureRequestInteraction(final HttpRequest request, final String body) throws JsonProcessingException {
        final String path = request.getURI().getPath();
        final String source = serviceNameDeriver.derive();
        final String destination = destinationNameMappings.mapForPath(path);
        final String interactionMessage = requestOf(request.getMethodValue(), path, source, destination);
        final var headers = request.getHeaders().entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> (Collection<String>) e.getValue()));
        final Map<String, Object> map = mapGenerator.generateFrom(body, headers, interactionMessage, REQUEST);
        final Document document = Document.parse(objectMapper.writeValueAsString(map));
        interceptedDocumentRepository.save(document);
    }
}