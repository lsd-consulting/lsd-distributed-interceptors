package com.integreety.yatspec.e2e.captor.repository.model;

import com.integreety.yatspec.e2e.teststate.dto.Interaction;
import lombok.Getter;

import java.util.function.Function;

import static com.integreety.yatspec.e2e.teststate.template.InteractionMessageTemplates.*;

@Getter
public enum Type {

    REQUEST(i -> requestOf(i.getHttpMethod(), i.getPath(), i.getSource(), i.getDestination())),
    RESPONSE(i -> responseOf(i.getHttpStatus(), i.getDestination(), i.getSource())),
    PUBLISH(i -> publishOf(i.getSource(), i.getDestination())),
    CONSUME(i -> consumeOf(i.getDestination(), i.getSource()));

    private final Function<Interaction, String> interactionName;

    Type(final Function<Interaction, String> interactionName) {
        this.interactionName = interactionName;
    }
}