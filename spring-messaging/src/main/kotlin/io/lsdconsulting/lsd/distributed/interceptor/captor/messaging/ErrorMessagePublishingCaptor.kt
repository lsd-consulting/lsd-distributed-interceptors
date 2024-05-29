package io.lsdconsulting.lsd.distributed.interceptor.captor.messaging

import io.lsdconsulting.lsd.distributed.connector.model.InteractionType.PUBLISH
import io.lsdconsulting.lsd.distributed.connector.model.InterceptedInteraction
import io.lsdconsulting.lsd.distributed.interceptor.captor.common.PropertyServiceNameDeriver
import io.lsdconsulting.lsd.distributed.interceptor.captor.common.print
import io.lsdconsulting.lsd.distributed.interceptor.captor.trace.TraceIdRetriever
import io.lsdconsulting.lsd.distributed.interceptor.persistence.RepositoryService
import org.springframework.integration.channel.PublishSubscribeChannel
import org.springframework.messaging.Message
import org.springframework.messaging.MessageChannel
import java.time.ZoneId
import java.time.ZonedDateTime

class ErrorMessagePublishingCaptor(
    private val repositoryService: RepositoryService,
    private val propertyServiceNameDeriver: PropertyServiceNameDeriver,
    private val traceIdRetriever: TraceIdRetriever,
    private val messagingHeaderRetriever: MessagingHeaderRetriever,
    private val profile: String,
) {
    fun capturePublishErrorInteraction(message: Message<*>, channel: MessageChannel): InterceptedInteraction {
        val source = propertyServiceNameDeriver.serviceName
        val target = getPlantUmlFriendlyName(channel)
        val headers = messagingHeaderRetriever.retrieve(message)
        val interceptedInteraction = InterceptedInteraction(
            traceId = traceIdRetriever.getTraceId(headers),
            body = print(message.payload) { obj ->
                serialiseWithAvro(obj)
            },
            requestHeaders = headers,
            responseHeaders = emptyMap(),
            serviceName = source,
            target = target, path = target,
            httpStatus = null, httpMethod = null,
            interactionType = PUBLISH, profile = profile, elapsedTime = 0L,
            createdAt = ZonedDateTime.now(ZoneId.of("UTC"))
        )
        repositoryService.enqueue(interceptedInteraction)
        return interceptedInteraction
    }

    private fun getPlantUmlFriendlyName(channel: MessageChannel) =
        (channel as PublishSubscribeChannel).fullChannelName
}
