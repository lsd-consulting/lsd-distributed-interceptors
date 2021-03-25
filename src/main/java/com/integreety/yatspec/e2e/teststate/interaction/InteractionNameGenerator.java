package com.integreety.yatspec.e2e.teststate.interaction;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.integreety.yatspec.e2e.captor.repository.model.InterceptedInteraction;
import com.integreety.yatspec.e2e.config.mapper.ObjectMapperCreator;
import com.integreety.yatspec.e2e.teststate.dto.Interaction;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;

import static com.integreety.yatspec.e2e.teststate.indent.JsonPrettyPrinter.indentJson;
import static com.integreety.yatspec.e2e.teststate.indent.XmlPrettyPrinter.indentXml;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;

@Slf4j
@RequiredArgsConstructor
public class InteractionNameGenerator {

    public static final String SOURCE_NAME_KEY = "Source-Name";
    public static final String TARGET_NAME_KEY = "Target-Name";

    private final ObjectMapper objectMapper = new ObjectMapperCreator().getObjectMapper();

    public List<Pair<String, Object>> generate(final List<InterceptedInteraction> interceptedInteractions) {

        final List<Pair<String, Object>> interactions = new ArrayList<>();
        for (final InterceptedInteraction interceptedInteraction : interceptedInteractions) {
            final String destination = deriveDestinationName(interceptedInteraction);
            final String source = deriveSourceName(interceptedInteraction);
            final String interactionName = interceptedInteraction.getType().getInteractionName().apply(buildInteraction(interceptedInteraction, source, destination));
            log.info("Generated an interaction name={}", interactionName);
            final String body = indent(interceptedInteraction.getBody());
            interactions.add(Pair.of(interactionName, body));
        }
        return interactions;
    }

    private String deriveSourceName(final InterceptedInteraction interceptedInteraction) {
        final var headers = interceptedInteraction.getRequestHeaders();
        return isNotEmpty(headers) && headers.containsKey(SOURCE_NAME_KEY)
                ? headers.get(SOURCE_NAME_KEY).stream().findFirst().orElse(interceptedInteraction.getServiceName()) : interceptedInteraction.getServiceName();
    }

    private String deriveDestinationName(final InterceptedInteraction interceptedInteraction) {
        final var headers = interceptedInteraction.getRequestHeaders();
        return isNotEmpty(headers) && headers.containsKey(TARGET_NAME_KEY)
                ? headers.get(TARGET_NAME_KEY).stream().findFirst().orElse(interceptedInteraction.getTarget()) : interceptedInteraction.getTarget();
    }

    @SneakyThrows
    private Interaction buildInteraction(final InterceptedInteraction interceptedInteraction, final String source, final String destination) {
        return Interaction.builder()
                .source(source)
                .destination(destination)
                .httpMethod(interceptedInteraction.getHttpMethod())
                .httpStatus(interceptedInteraction.getHttpStatus())
                .path(interceptedInteraction.getTarget())
                .createdAt(objectMapper.writeValueAsString(interceptedInteraction.getCreatedAt()))
                .build();
    }

    private String indent(final String document) {
        return indentJson(document).orElseGet(() -> indentXml(document).orElse(document));
    }
}