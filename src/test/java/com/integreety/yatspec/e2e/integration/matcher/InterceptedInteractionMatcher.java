package com.integreety.yatspec.e2e.integration.matcher;

import com.integreety.yatspec.e2e.captor.repository.model.InterceptedInteraction;
import com.integreety.yatspec.e2e.captor.repository.model.Type;
import lombok.RequiredArgsConstructor;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;

@RequiredArgsConstructor
public class InterceptedInteractionMatcher extends TypeSafeMatcher<InterceptedInteraction> {

    private final Type type;
    private final String serviceName;
    private final String body;
    private final String target;
    private final String path;

    @Override
    protected boolean matchesSafely(final InterceptedInteraction interceptedInteraction) {
        return interceptedInteraction.getType().equals(type)
                && interceptedInteraction.getServiceName().equals(serviceName)
                && interceptedInteraction.getBody().equals(body)
                && interceptedInteraction.getTarget().equals(target)
                && interceptedInteraction.getPath().equals(path);
    }

    @Override
    public void describeTo(final Description description) {
        description.appendText("an item with body:" + body + " and serviceName:" + serviceName + " and type:" + type + " and target:" + target + " and path:" + path);
    }

    public static InterceptedInteractionMatcher with(final Type type, final String serviceName, final String body, final String target, final String path) {
        return new InterceptedInteractionMatcher(type, serviceName, body, target, path);
    }
}
