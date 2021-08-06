package io.lsdconsulting.lsd.distributed.diagram.interaction;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lsd.diagram.ValidComponentName;
import io.lsdconsulting.lsd.distributed.captor.repository.model.InterceptedInteraction;
import io.lsdconsulting.lsd.distributed.config.mapper.ObjectMapperCreator;
import io.lsdconsulting.lsd.distributed.diagram.dto.Interaction;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;

import java.util.*;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY;
import static io.lsdconsulting.lsd.distributed.captor.repository.model.Type.*;
import static java.util.Optional.ofNullable;
import static lsd.format.PrettyPrinter.prettyPrintJson;

@Slf4j
@RequiredArgsConstructor
public class InteractionGenerator {

    private final ObjectMapper objectMapper = new ObjectMapperCreator().getObjectMapper();

    public List<Pair<String, String>> generate(final List<InterceptedInteraction> interceptedInteractions, final Map<String, Optional<String>> traceIdToColourMap) {

        final List<Pair<String, String>> interactions = new ArrayList<>();
        for (final InterceptedInteraction interceptedInteraction : interceptedInteractions) {
            final String colour = ofNullable(traceIdToColourMap.get(interceptedInteraction.getTraceId())).flatMap(x -> x).orElse("");
            final String lsdInteraction = interceptedInteraction.getType().getInteractionName().apply(buildInteraction(interceptedInteraction, colour));
            log.info("Generated an interaction name={}", lsdInteraction);
            final String body = prettyPrintJson(buildInteractionBody(interceptedInteraction));
            interactions.add(Pair.of(lsdInteraction, body));
        }
        return interactions;
    }

    private InteractionBody buildInteractionBody(InterceptedInteraction interceptedInteraction) {
        return InteractionBody.builder()
                .requestHeaders(interceptedInteraction.getType().equals(REQUEST) ? interceptedInteraction.getRequestHeaders() : null)
                .responseHeaders(interceptedInteraction.getType().equals(RESPONSE) ? interceptedInteraction.getResponseHeaders() : null)
                .headers(List.of(PUBLISH, CONSUME).contains(interceptedInteraction.getType()) ? interceptedInteraction.getRequestHeaders() : null)
                .body(interceptedInteraction.getBody())
                .build();
    }

    @Value
    @Builder
    static class InteractionBody {
        @JsonInclude(NON_EMPTY)
        Map<String, Collection<String>> requestHeaders;

        @JsonInclude(NON_EMPTY)
        Map<String, Collection<String>> responseHeaders;

        @JsonInclude(NON_EMPTY)
        Map<String, Collection<String>> headers;

        String body;
    }

    @SneakyThrows
    private Interaction buildInteraction(final InterceptedInteraction interceptedInteraction, final String colour) {
        return Interaction.builder()
                .source(ValidComponentName.of(interceptedInteraction.getServiceName()))
                .destination(ValidComponentName.of(interceptedInteraction.getTarget()))
                .httpMethod(interceptedInteraction.getHttpMethod())
                .httpStatus(interceptedInteraction.getHttpStatus())
                .path(interceptedInteraction.getPath())
                .createdAt(objectMapper.writeValueAsString(interceptedInteraction.getCreatedAt()))
                .colour(colour)
                .elapsedTime(ofNullable(interceptedInteraction.getElapsedTime()).map(Object::toString).orElse(null))
                .build();
    }

}