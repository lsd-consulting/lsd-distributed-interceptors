package io.lsdconsulting.lsd.distributed.interceptor.captor.http

import feign.Request
import io.lsdconsulting.lsd.distributed.access.model.InteractionType
import io.lsdconsulting.lsd.distributed.access.model.InterceptedInteraction
import io.lsdconsulting.lsd.distributed.interceptor.captor.convert.convert
import io.lsdconsulting.lsd.distributed.interceptor.captor.http.derive.PathDeriver
import io.lsdconsulting.lsd.distributed.interceptor.captor.http.derive.SourceTargetDeriver
import io.lsdconsulting.lsd.distributed.interceptor.captor.trace.TraceIdRetriever
import io.lsdconsulting.lsd.distributed.interceptor.persistance.RepositoryService
import org.springframework.http.HttpRequest
import java.time.ZoneId
import java.time.ZonedDateTime

class RequestCaptor(
    private val repositoryService: RepositoryService,
    private val sourceTargetDeriver: SourceTargetDeriver,
    private val pathDeriver: PathDeriver,
    private val traceIdRetriever: TraceIdRetriever,
    private val httpHeaderRetriever: HttpHeaderRetriever,
    private val profile: String,
){
    fun captureRequestInteraction(request: Request): InterceptedInteraction {
        val headers = httpHeaderRetriever.retrieve(request)
        val body = request.body()?.convert()
        val path = pathDeriver.derivePathFrom(request.url())
        val traceId = traceIdRetriever.getTraceId(headers)
        val target = sourceTargetDeriver.deriveTarget(headers, path)
        val serviceName = sourceTargetDeriver.deriveServiceName(headers)
        val interceptedInteraction =
            buildInterceptedInteraction(headers, body, path, traceId, target, serviceName, request.httpMethod().name)
        repositoryService.enqueue(interceptedInteraction)
        return interceptedInteraction
    }

    fun captureRequestInteraction(request: HttpRequest, body: String?): InterceptedInteraction {
        val headers = httpHeaderRetriever.retrieve(request)
        val path = pathDeriver.derivePathFrom(request)
        val traceId = traceIdRetriever.getTraceId(headers)
        val target = sourceTargetDeriver.deriveTarget(headers, path)
        val serviceName = sourceTargetDeriver.deriveServiceName(headers)
        val interceptedInteraction =
            buildInterceptedInteraction(headers, body, path, traceId, target, serviceName, request.methodValue)
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
