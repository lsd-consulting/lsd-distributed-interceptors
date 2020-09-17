package com.integreety.yatspec.e2e.captor.template;

/**
 * Useful to keep the interaction messages consistent across various interceptors.
 */
public class InteractionMessageTemplates {

    public static final String REQUEST_TEMPLATE = "%s %s from %s to %s";
    public static final String RESPONSE_TEMPLATE = "%s response from %s to %s";
    public static final String CONSUME_TEMPLATE = "consume message from %s to %s";
    public static final String PUBLISH_TEMPLATE = "publish event from %s to %s";

    public static String requestOf(final String method, final String path, final String sourceName, final String destinationName) {
        return String.format(REQUEST_TEMPLATE, method, path, sourceName, destinationName);
    }

    public static String responseOf(final String message, final String destinationName, final String sourceName) {
        return String.format(RESPONSE_TEMPLATE, message, destinationName, sourceName);
    }

    public static String consumeOf(final String exchangeName, final String serviceName) {
        return String.format(CONSUME_TEMPLATE, exchangeName, serviceName);
    }

    public static String publishOf(final String serviceName, final String exchangeName) {
        return String.format(PUBLISH_TEMPLATE, serviceName, exchangeName);
    }
}