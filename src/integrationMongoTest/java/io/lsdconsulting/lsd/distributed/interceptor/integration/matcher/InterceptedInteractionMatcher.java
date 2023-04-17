package io.lsdconsulting.lsd.distributed.interceptor.integration.matcher;

import io.lsdconsulting.lsd.distributed.access.model.InteractionType;
import io.lsdconsulting.lsd.distributed.access.model.InterceptedInteraction;
import lombok.RequiredArgsConstructor;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;

@RequiredArgsConstructor
public class InterceptedInteractionMatcher extends TypeSafeMatcher<InterceptedInteraction> {

    private final InteractionType type;
    private final String serviceName;
    private final String body;
    private final String target;
    private final String path;

    @Override
    protected boolean matchesSafely(final InterceptedInteraction interceptedInteraction) {
        return interceptedInteraction.getInteractionType().equals(type)
                && interceptedInteraction.getServiceName().equals(serviceName)
                && interceptedInteraction.getBody().equals(body)
                && interceptedInteraction.getTarget().equals(target)
                && interceptedInteraction.getPath().equals(path);
    }

    @Override
    public void describeTo(final Description description) {
        description.appendText("an item with body:" + body + " and serviceName:" + serviceName + " and type:" + type + " and target:" + target + " and path:" + path);
    }

    public static InterceptedInteractionMatcher with(final InteractionType type, final String serviceName, final String body, final String target, final String path) {
        return new InterceptedInteractionMatcher(type, serviceName, body, target, path);
    }
}
