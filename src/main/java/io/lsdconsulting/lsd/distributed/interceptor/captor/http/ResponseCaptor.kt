package io.lsdconsulting.lsd.distributed.interceptor.captor.http

import feign.Response
import io.lsdconsulting.lsd.distributed.access.model.InteractionType
import io.lsdconsulting.lsd.distributed.access.model.InterceptedInteraction
import io.lsdconsulting.lsd.distributed.interceptor.captor.convert.TypeConverter.convert
import io.lsdconsulting.lsd.distributed.interceptor.captor.http.derive.HttpStatusDeriver
import io.lsdconsulting.lsd.distributed.interceptor.captor.http.derive.PathDeriver
import io.lsdconsulting.lsd.distributed.interceptor.captor.http.derive.SourceTargetDeriver
import io.lsdconsulting.lsd.distributed.interceptor.captor.trace.TraceIdRetriever
import io.lsdconsulting.lsd.distributed.interceptor.persistance.RepositoryService
import org.apache.commons.lang3.StringUtils
import org.springframework.http.HttpRequest
import org.springframework.http.client.ClientHttpResponse
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.time.ZoneId
import java.time.ZonedDateTime

class ResponseCaptor(
    private val repositoryService: RepositoryService,
    private val sourceTargetDeriver: SourceTargetDeriver,
    private val pathDeriver: PathDeriver,
    private val traceIdRetriever: TraceIdRetriever,
    private val httpHeaderRetriever: HttpHeaderRetriever,
    private val httpStatusDeriver: HttpStatusDeriver,
    private val profile: String,
){
    fun captureResponseInteraction(response: Response, elapsedTime: Long): InterceptedInteraction {
        val requestHeaders = httpHeaderRetriever.retrieve(response.request())
        val responseHeaders = httpHeaderRetriever.retrieve(response)
        val path = pathDeriver.derivePathFrom(response.request().url())
        val target = sourceTargetDeriver.deriveTarget(requestHeaders, path)
        val serviceName = sourceTargetDeriver.deriveServiceName(requestHeaders)
        val traceId = traceIdRetriever.getTraceId(requestHeaders)
        val httpStatus = httpStatusDeriver.derive(response.status())
        val interceptedInteraction = buildInterceptedInteraction(
            target,
            path,
            traceId,
            elapsedTime,
            requestHeaders,
            responseHeaders,
            serviceName,
            convert(response.body()),
            httpStatus
        )
        repositoryService.enqueue(interceptedInteraction)
        return interceptedInteraction
    }

    @Throws(IOException::class)
    fun captureResponseInteraction(
        request: HttpRequest?,
        response: ClientHttpResponse,
        target: String,
        path: String,
        traceId: String,
        elapsedTime: Long
    ): InterceptedInteraction {
        val requestHeaders = httpHeaderRetriever.retrieve(request)
        val responseHeaders = httpHeaderRetriever.retrieve(response)
        val serviceName = sourceTargetDeriver.deriveServiceName(requestHeaders)
        val body = copyBodyToString(response)
        val httpStatus = response.statusCode.toString()
        val interceptedInteraction = buildInterceptedInteraction(
            target,
            path,
            traceId,
            elapsedTime,
            requestHeaders,
            responseHeaders,
            serviceName,
            body,
            httpStatus
        )
        repositoryService.enqueue(interceptedInteraction)
        return interceptedInteraction
    }

    private fun buildInterceptedInteraction(
        target: String,
        path: String,
        traceId: String,
        elapsedTime: Long,
        requestHeaders: MutableMap<String, Collection<String>>,
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

    @Throws(IOException::class)
    private fun copyBodyToString(response: ClientHttpResponse): String {
        if (response.headers.contentLength == 0L) {
            return StringUtils.EMPTY
        }
        val outputStream = ByteArrayOutputStream()
        val inputStream = response.body
        inputStream.transferTo(outputStream)
        return outputStream.toString()
    }
}
