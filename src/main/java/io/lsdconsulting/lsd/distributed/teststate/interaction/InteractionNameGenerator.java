package io.lsdconsulting.lsd.distributed.teststate.interaction;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lsd.diagram.ValidComponentName;
import io.lsdconsulting.lsd.distributed.captor.repository.model.InterceptedInteraction;
import io.lsdconsulting.lsd.distributed.config.mapper.ObjectMapperCreator;
import io.lsdconsulting.lsd.distributed.teststate.dto.Interaction;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static lsd.format.Formatter.indent;

@Slf4j
@RequiredArgsConstructor
public class InteractionNameGenerator {

    private final ObjectMapper objectMapper = new ObjectMapperCreator().getObjectMapper();

    public List<Pair<String, String>> generate(final List<InterceptedInteraction> interceptedInteractions, final Map<String, Optional<String>> traceIdToColourMap) {

        final List<Pair<String, String>> interactions = new ArrayList<>();
        for (final InterceptedInteraction interceptedInteraction : interceptedInteractions) {
            final String colour = traceIdToColourMap.get(interceptedInteraction.getTraceId()).orElse("");
            final String interactionName = interceptedInteraction.getType().getInteractionName().apply(buildInteraction(interceptedInteraction, colour));
            log.info("Generated an interaction name={}", interactionName);
            final String body = indent(interceptedInteraction.getBody());
            interactions.add(Pair.of(interactionName, body));
        }
        return interactions;
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
                .build();
    }

}