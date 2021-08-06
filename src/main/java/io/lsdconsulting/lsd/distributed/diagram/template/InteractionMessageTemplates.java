package io.lsdconsulting.lsd.distributed.diagram.template;

import lombok.NoArgsConstructor;

import static lombok.AccessLevel.PRIVATE;

/**
 * Useful to keep the interaction messages consistent across various interceptors.
 */
@NoArgsConstructor(access = PRIVATE)
public class InteractionMessageTemplates {

    public static final String REQUEST_TEMPLATE = "%s %s from %s to %s %s";
    public static final String RESPONSE_TEMPLATE = "sync %s response (%s ms) from %s to %s %s";
    public static final String PUBLISH_TEMPLATE = "publish event from %s to %s %s";
    public static final String CONSUME_TEMPLATE = "consume message from %s to %s %s";

    public static String requestOf(final String method, final String path, final String sourceName, final String destinationName, final String colour) {
        return String.format(REQUEST_TEMPLATE, method, path, sourceName, destinationName, colour).trim();
    }

    public static String responseOf(final String status, final String destinationName, final String sourceName, final String elapsedTime, final String colour) {
        return String.format(RESPONSE_TEMPLATE, status, elapsedTime, destinationName, sourceName, colour).trim();
    }

    public static String publishOf(final String serviceName, final String exchangeName, final String colour) {
        return String.format(PUBLISH_TEMPLATE, serviceName, exchangeName, colour).trim();
    }

    public static String consumeOf(final String exchangeName, final String serviceName, final String colour) {
        return String.format(CONSUME_TEMPLATE, exchangeName, serviceName, colour).trim();
    }
}