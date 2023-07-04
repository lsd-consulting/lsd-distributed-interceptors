package io.lsdconsulting.lsd.distributed.interceptor.integration.matcher

import io.lsdconsulting.lsd.distributed.connector.model.InteractionType
import io.lsdconsulting.lsd.distributed.connector.model.InterceptedInteraction
import org.hamcrest.Description
import org.hamcrest.TypeSafeMatcher

class InterceptedInteractionMatcher(
    private val interactionType: InteractionType,
    private val serviceName: String,
    private val body: String?,
    private val target: String,
    private val path: String,
    private val httpStatus: String?,
    private val httpMethod: String?,
) : TypeSafeMatcher<InterceptedInteraction>() {

    override fun matchesSafely(interceptedInteraction: InterceptedInteraction): Boolean {
        return interceptedInteraction.interactionType == interactionType &&
                interceptedInteraction.serviceName == serviceName &&
                interceptedInteraction.body == body &&
                interceptedInteraction.target == target &&
                interceptedInteraction.path == path &&
                interceptedInteraction.httpStatus == httpStatus &&
                interceptedInteraction.httpMethod == httpMethod
    }

    override fun describeTo(description: Description) {
        description.appendText("an item with body:$body and serviceName:$serviceName and type:$interactionType and target:$target and path:$path")
    }

    companion object {
        fun with(
            interactionType: InteractionType,
            serviceName: String,
            body: String?,
            target: String,
            path: String,
            httpStatus: String?,
            httpMethod: String?,
        ): InterceptedInteractionMatcher {
            return InterceptedInteractionMatcher(interactionType, serviceName, body, target, path, httpStatus, httpMethod)
        }
    }
}
