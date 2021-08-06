package io.lsdconsulting.lsd.distributed.captor.repository.model;

import io.lsdconsulting.lsd.distributed.diagram.dto.Interaction;
import io.lsdconsulting.lsd.distributed.diagram.template.InteractionMessageTemplates;
import lombok.Getter;

import java.util.function.Function;

@Getter
public enum Type {

    REQUEST(i -> InteractionMessageTemplates.requestOf(i.getHttpMethod(), i.getPath(), i.getSource(), i.getDestination(), i.getColour())),
    RESPONSE(i -> InteractionMessageTemplates.responseOf(i.getHttpStatus(), i.getDestination(), i.getSource(), i.getElapsedTime(), i.getColour())),
    PUBLISH(i -> InteractionMessageTemplates.publishOf(i.getSource(), i.getDestination(), i.getColour())),
    CONSUME(i -> InteractionMessageTemplates.consumeOf(i.getDestination(), i.getSource(), i.getColour()));

    private final Function<Interaction, String> interactionName;

    Type(final Function<Interaction, String> interactionName) {
        this.interactionName = interactionName;
    }
}