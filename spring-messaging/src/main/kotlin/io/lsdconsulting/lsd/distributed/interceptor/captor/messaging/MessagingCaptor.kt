package io.lsdconsulting.lsd.distributed.interceptor.captor.messaging

import io.lsdconsulting.lsd.distributed.connector.model.InteractionType.CONSUME
import io.lsdconsulting.lsd.distributed.connector.model.InteractionType.PUBLISH
import io.lsdconsulting.lsd.distributed.connector.model.InterceptedInteraction
import io.lsdconsulting.lsd.distributed.interceptor.captor.common.PropertyServiceNameDeriver
import io.lsdconsulting.lsd.distributed.interceptor.captor.common.print
import io.lsdconsulting.lsd.distributed.interceptor.captor.http.SourceTargetDeriver.Companion.SOURCE_NAME_KEY
import io.lsdconsulting.lsd.distributed.interceptor.captor.http.SourceTargetDeriver.Companion.TARGET_NAME_KEY
import io.lsdconsulting.lsd.distributed.interceptor.captor.trace.TraceIdRetriever
import io.lsdconsulting.lsd.distributed.interceptor.config.log
import io.lsdconsulting.lsd.distributed.interceptor.persistence.RepositoryService
import org.springframework.messaging.Message
import java.time.ZoneId
import java.time.ZonedDateTime

class MessagingCaptor(
    private val repositoryService: RepositoryService,
    private val propertyServiceNameDeriver: PropertyServiceNameDeriver,
    private val traceIdRetriever: TraceIdRetriever,
    private val messagingHeaderRetriever: MessagingHeaderRetriever,
    private val profile: String,
) {
    fun captureConsumeInteraction(message: Message<*>): InterceptedInteraction {
        val headers = messagingHeaderRetriever.retrieve(message)
        val interceptedInteraction = InterceptedInteraction(
            traceId = traceIdRetriever.getTraceId(headers),
            body = print(message.payload),
            requestHeaders = headers,
            responseHeaders = emptyMap(),
            serviceName = propertyServiceNameDeriver.serviceName,
            target = getTarget(message),
            path = propertyServiceNameDeriver.serviceName,
            httpStatus = null, httpMethod = null,
            interactionType = CONSUME, profile = profile, elapsedTime = 0L,
            createdAt = ZonedDateTime.now(ZoneId.of("UTC"))
        )
        repositoryService.enqueue(interceptedInteraction)
        return interceptedInteraction
    }

    // TODO Add support for info.app.name
    private fun getTarget(message: Message<*>): String {
        val header = message.headers[TARGET_NAME_KEY]
        var target = if (header is String) header else if (header is ByteArray) String(header) else null
        if (target.isNullOrEmpty()) {
            val typeIdHeader = message.headers["__TypeId__"] as String?
            target = getTargetFrom(typeIdHeader)
        }
        if (target.isNullOrEmpty()) {
            target = "UNKNOWN"
        }
        log().debug("found target:{}", target)
        return target
    }

    private fun getTargetFrom(typeIdHeader: String?): String? {
        return if (!typeIdHeader.isNullOrBlank()) {
            return typeIdHeader.split("\\.".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray<String>().last()
        }
        else null
    }

    fun capturePublishInteraction(message: Message<*>): InterceptedInteraction {
        val source = message.headers[SOURCE_NAME_KEY] as String?
        val target = message.headers[TARGET_NAME_KEY] as String
        val headers = messagingHeaderRetriever.retrieve(message)
        val interceptedInteraction = InterceptedInteraction(
            traceId = traceIdRetriever.getTraceId(headers),
            body = print(message.payload),
            requestHeaders = headers,
            responseHeaders = emptyMap(),
            serviceName = source ?: propertyServiceNameDeriver.serviceName,
            target = target, path = target,
            httpStatus = null, httpMethod = null,
            interactionType = PUBLISH, profile = profile, elapsedTime = 0L,
            createdAt = ZonedDateTime.now(ZoneId.of("UTC"))
        )
        repositoryService.enqueue(interceptedInteraction)
        return interceptedInteraction
    }
}
