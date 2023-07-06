package io.lsdconsulting.lsd.distributed.interceptor.captor.messaging

import io.lsdconsulting.lsd.distributed.connector.model.InteractionType
import io.lsdconsulting.lsd.distributed.connector.model.InteractionType.PUBLISH
import io.lsdconsulting.lsd.distributed.connector.model.InterceptedInteraction
import io.lsdconsulting.lsd.distributed.interceptor.captor.common.PropertyServiceNameDeriver
import io.lsdconsulting.lsd.distributed.interceptor.captor.common.stringify
import io.lsdconsulting.lsd.distributed.interceptor.captor.http.SourceTargetDeriver.Companion.SOURCE_NAME_KEY
import io.lsdconsulting.lsd.distributed.interceptor.captor.http.SourceTargetDeriver.Companion.TARGET_NAME_KEY
import io.lsdconsulting.lsd.distributed.interceptor.captor.trace.TraceIdRetriever
import io.lsdconsulting.lsd.distributed.interceptor.config.log
import io.lsdconsulting.lsd.distributed.interceptor.persistance.RepositoryService
import lsd.format.PrettyPrinter
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
            body = PrettyPrinter.prettyPrint(message.payload),
            requestHeaders = headers,
            responseHeaders = emptyMap(),
            serviceName = propertyServiceNameDeriver.serviceName,
            target = getSource(message),
            path = propertyServiceNameDeriver.serviceName,
            httpStatus = null,
            httpMethod = null, interactionType = InteractionType.CONSUME,
            profile = profile,
            elapsedTime = 0L,
            createdAt = ZonedDateTime.now(ZoneId.of("UTC"))
        )
        repositoryService.enqueue(interceptedInteraction)
        return interceptedInteraction
    }

    private fun getSource(message: Message<*>): String {
        val header = message.headers[TARGET_NAME_KEY]
        var source = if (header is String) header else if (header is ByteArray) String(header) else null
        if (source.isNullOrEmpty()) {
            val typeIdHeader = message.headers["__TypeId__"] as String?
            source = getSourceFrom(typeIdHeader)
        }
        if (source.isNullOrEmpty()) {
            source = "UNKNOWN"
        }
        log().debug("found source:{}", source)
        return source
    }

    private fun getSourceFrom(typeIdHeader: String?): String? {
        return if (!typeIdHeader.isNullOrBlank()) {
            val sourceTokens = typeIdHeader.split("\\.".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            return sourceTokens[sourceTokens.size - 1]
        }
        else null
    }

    fun capturePublishInteraction(message: Message<*>): InterceptedInteraction {
        val source = message.headers[SOURCE_NAME_KEY] as String?
        val target = message.headers[TARGET_NAME_KEY] as String
        val headers = messagingHeaderRetriever.retrieve(message)
        val interceptedInteraction = InterceptedInteraction(
            traceId = traceIdRetriever.getTraceId(headers),
            body = (message.payload as ByteArray).stringify(),
            requestHeaders = headers,
            responseHeaders = emptyMap(),
            serviceName = source ?: propertyServiceNameDeriver.serviceName,
            target = target, path = target,
            httpStatus = null, httpMethod = null,
            interactionType = PUBLISH, profile = profile,
            elapsedTime = 0L, createdAt = ZonedDateTime.now(ZoneId.of("UTC"))
        )
        repositoryService.enqueue(interceptedInteraction)
        return interceptedInteraction
    }
}
