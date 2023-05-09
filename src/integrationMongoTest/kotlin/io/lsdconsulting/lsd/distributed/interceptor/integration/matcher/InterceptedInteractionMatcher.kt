package io.lsdconsulting.lsd.distributed.interceptor.integration.matcher

import io.lsdconsulting.lsd.distributed.access.model.InteractionType
import io.lsdconsulting.lsd.distributed.access.model.InterceptedInteraction
import org.hamcrest.Description
import org.hamcrest.TypeSafeMatcher

class InterceptedInteractionMatcher(
    private val type: InteractionType,
    private val serviceName: String,
    private val body: String,
    private val target: String,
    private val path: String,
) : TypeSafeMatcher<InterceptedInteraction>() {

    override fun matchesSafely(interceptedInteraction: InterceptedInteraction): Boolean {
        return interceptedInteraction.interactionType == type && interceptedInteraction.serviceName == serviceName && interceptedInteraction.body == body && interceptedInteraction.target == target && interceptedInteraction.path == path
    }

    override fun describeTo(description: Description) {
        description.appendText("an item with body:$body and serviceName:$serviceName and type:$type and target:$target and path:$path")
    }

    companion object {
        fun with(
            type: InteractionType,
            serviceName: String,
            body: String,
            target: String,
            path: String
        ): InterceptedInteractionMatcher {
            return InterceptedInteractionMatcher(type, serviceName, body, target, path)
        }
    }
}