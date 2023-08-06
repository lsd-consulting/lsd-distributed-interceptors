package io.lsdconsulting.lsd.distributed.interceptor.captor.rabbit

import io.lsdconsulting.lsd.distributed.connector.model.InteractionType
import io.lsdconsulting.lsd.distributed.connector.model.InterceptedInteraction
import io.lsdconsulting.lsd.distributed.interceptor.captor.common.PropertyServiceNameDeriver
import io.lsdconsulting.lsd.distributed.interceptor.captor.common.print
import io.lsdconsulting.lsd.distributed.interceptor.captor.trace.TraceIdRetriever
import io.lsdconsulting.lsd.distributed.interceptor.persistence.RepositoryService
import org.springframework.amqp.core.Message
import java.time.ZoneId
import java.time.ZonedDateTime

class RabbitCaptor(
    private val repositoryService: RepositoryService,
    private val propertyServiceNameDeriver: PropertyServiceNameDeriver,
    private val traceIdRetriever: TraceIdRetriever,
    private val amqpHeaderRetriever: AmqpHeaderRetriever,
    private val profile: String,
) {
    fun captureInteraction(exchange: String?, message: Message, type: InteractionType?): InterceptedInteraction {
        val headers = amqpHeaderRetriever.retrieve(message)
        val interceptedInteraction = InterceptedInteraction(
            traceId = traceIdRetriever.getTraceId(headers),
            body = print(message.body),
            requestHeaders = headers,
            responseHeaders = emptyMap(),
            serviceName = propertyServiceNameDeriver.serviceName,
            target = exchange!!,
            path = exchange,
            httpStatus = null,
            httpMethod = null,
            interactionType = type!!,
            profile = profile,
            elapsedTime = 0L,
            createdAt = ZonedDateTime.now(ZoneId.of("UTC"))
        )
        repositoryService.enqueue(interceptedInteraction)
        return interceptedInteraction
    }
}
