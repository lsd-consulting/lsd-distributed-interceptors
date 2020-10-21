package com.integreety.yatspec.e2e.integration.matcher;

import com.integreety.yatspec.e2e.captor.repository.model.InterceptedCall;
import com.integreety.yatspec.e2e.captor.repository.model.Type;
import lombok.RequiredArgsConstructor;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;

@RequiredArgsConstructor
public class InterceptedCallMatcher extends TypeSafeMatcher<InterceptedCall> {

    private final Type type;
    private final String serviceName;
    private final String body;
    private final String target;

    @Override
    protected boolean matchesSafely(final InterceptedCall interceptedCall) {
        return interceptedCall.getType().equals(type)
                && interceptedCall.getServiceName().equals(serviceName)
                && interceptedCall.getBody().equals(body)
                && interceptedCall.getTarget().equals(target);
    }

    @Override
    public void describeTo(final Description description) {
        description.appendText("an item with body:" + body + " and serviceName:" + serviceName + " and type:" + type + " and target:" + target);
    }

    public static InterceptedCallMatcher with(final Type type, final String serviceName, final String body, final String target) {
        return new InterceptedCallMatcher(type, serviceName, body, target);
    }
}
