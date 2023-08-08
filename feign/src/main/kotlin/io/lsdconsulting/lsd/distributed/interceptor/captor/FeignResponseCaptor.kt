package io.lsdconsulting.lsd.distributed.interceptor.captor

import feign.Response
import io.lsdconsulting.lsd.distributed.connector.model.InteractionType
import io.lsdconsulting.lsd.distributed.connector.model.InterceptedInteraction
import io.lsdconsulting.lsd.distributed.interceptor.captor.http.SourceTargetDeriver
import io.lsdconsulting.lsd.distributed.interceptor.captor.http.toHttpStatus
import io.lsdconsulting.lsd.distributed.interceptor.captor.http.toPath
import io.lsdconsulting.lsd.distributed.interceptor.captor.trace.TraceIdRetriever
import io.lsdconsulting.lsd.distributed.interceptor.config.log
import io.lsdconsulting.lsd.distributed.interceptor.persistence.RepositoryService
import java.time.ZoneId
import java.time.ZonedDateTime

class FeignResponseCaptor(
    private val repositoryService: RepositoryService,
    private val sourceTargetDeriver: SourceTargetDeriver,
    private val traceIdRetriever: TraceIdRetriever,
    private val feignHttpHeaderRetriever: FeignHttpHeaderRetriever,
    private val profile: String,
) {
    fun captureResponseInteraction(response: Response, body: String?, elapsedTime: Long): InterceptedInteraction {
        log().debug("feignHttpHeaderRetriever={}", feignHttpHeaderRetriever)
        val responseHeaders = feignHttpHeaderRetriever.retrieve(response)
        val requestHeaders = feignHttpHeaderRetriever.retrieve(response.request())
        val path = response.request().url().toPath()
        val target = sourceTargetDeriver.deriveTarget(requestHeaders, path)
        val serviceName = sourceTargetDeriver.deriveServiceName(requestHeaders)
        val traceId = traceIdRetriever.getTraceId(requestHeaders)
        val httpStatus = response.status().toHttpStatus()
        val interceptedInteraction = buildInterceptedInteraction(
            target = target,
            path = path,
            traceId = traceId,
            elapsedTime = elapsedTime,
            requestHeaders = requestHeaders,
            responseHeaders = responseHeaders,
            serviceName = serviceName,
            body = body,
            httpStatus = httpStatus
        )
        repositoryService.enqueue(interceptedInteraction)
        return interceptedInteraction
    }

    private fun buildInterceptedInteraction(
        target: String,
        path: String,
        traceId: String,
        elapsedTime: Long,
        requestHeaders: Map<String, Collection<String>>,
        responseHeaders: Map<String, Collection<String>>,
        serviceName: String,
        body: String?,
        httpStatus: String?
    ) = InterceptedInteraction(
        traceId = traceId,
        body = body,
        requestHeaders = requestHeaders,
        responseHeaders = responseHeaders,
        serviceName = serviceName,
        target = target,
        path = path,
        httpStatus = httpStatus,
        httpMethod = null, interactionType = InteractionType.RESPONSE,
        profile = profile,
        elapsedTime = elapsedTime,
        createdAt = ZonedDateTime.now(ZoneId.of("UTC"))
    )
}
