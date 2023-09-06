package io.lsdconsulting.lsd.distributed.interceptor.captor.trace

import brave.Tracer
import lsd.logging.log

private const val B3_HEADER = "b3"
private const val X_REQUEST_INFO_HEADER = "X-Request-Info"

class TraceIdRetriever(
    private val tracer: Tracer
) {
    fun getTraceId(headers: Map<String, Collection<String>>): String {
        log().debug("headers received={}", headers)
        val traceId = getTraceIdFromB3Header(headers[B3_HEADER]) ?: getTraceIdFromXRequestInfo(headers[X_REQUEST_INFO_HEADER]) ?: traceIdFromTracer()
        log().debug("traceId retrieved={}", traceId)
        return traceId
    }

    /*
     * The advantage of this approach is that it will create a new traceId and hopefully pass it on with the next request.
     */
    private fun traceIdFromTracer() =
        if (tracer.currentSpan() == null) tracer.nextSpan().context()
            .traceIdString()
        else tracer.currentSpan().context().traceIdString()

    private fun getTraceIdFromXRequestInfo(xRequestInfoHeader: Collection<String>?) =
        xRequestInfoHeader?.first()?.split(";")?.map { it.trim() }
            ?.firstOrNull { it.startsWith("referenceId") }
            ?.split("=")?.drop(1)?.first()

    private fun getTraceIdFromB3Header(b3Header: Collection<String>?) =
        b3Header?.first()?.split("-")?.first()
}
