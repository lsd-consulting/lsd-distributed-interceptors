package io.lsdconsulting.lsd.distributed.interceptor.captor

import io.lsdconsulting.lsd.distributed.connector.model.InteractionType
import io.lsdconsulting.lsd.distributed.connector.model.InterceptedInteraction
import io.lsdconsulting.lsd.distributed.interceptor.captor.http.SourceTargetDeriver
import io.lsdconsulting.lsd.distributed.interceptor.captor.trace.TraceIdRetriever
import io.lsdconsulting.lsd.distributed.interceptor.persistence.RepositoryService
import org.springframework.http.HttpRequest
import java.time.ZoneId
import java.time.ZonedDateTime

open class RequestCaptor(
    private val repositoryService: RepositoryService,
    private val sourceTargetDeriver: SourceTargetDeriver,
    private val traceIdRetriever: TraceIdRetriever,
    private val httpHeaderRetriever: HttpHeaderRetriever,
    private val profile: String,
){

    open fun captureRequestInteraction(request: HttpRequest, body: String?): InterceptedInteraction {
        val headers = httpHeaderRetriever.retrieve(request)
        val path = request.toPath()
        val traceId = traceIdRetriever.getTraceId(headers)
        val target = sourceTargetDeriver.deriveTarget(headers, path)
        val serviceName = sourceTargetDeriver.deriveServiceName(headers)
        val interceptedInteraction =
            buildInterceptedInteraction(headers, body, path, traceId, target, serviceName, request.method.name())
        repositoryService.enqueue(interceptedInteraction)
        return interceptedInteraction
    }

    private fun buildInterceptedInteraction(
        headers: Map<String, Collection<String>>,
        body: String?,
        path: String,
        traceId: String,
        target: String,
        serviceName: String,
        httpMethod: String?
    ) = InterceptedInteraction(
        traceId = traceId,
        body = body,
        requestHeaders = headers,
        responseHeaders = emptyMap(),
        serviceName = serviceName,
        target = target,
        path = path,
        httpStatus = null,
        httpMethod = httpMethod, interactionType = InteractionType.REQUEST,
        profile = profile,
        elapsedTime = 0L,
        createdAt = ZonedDateTime.now(ZoneId.of("UTC"))
    )
}
