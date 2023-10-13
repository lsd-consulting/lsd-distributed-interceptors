package io.lsdconsulting.lsd.distributed.interceptor.interceptor

import feign.InvocationContext
import feign.Response
import feign.ResponseInterceptor
import io.lsdconsulting.lsd.distributed.interceptor.captor.FeignResponseCaptor
import io.lsdconsulting.lsd.distributed.interceptor.captor.common.print
import lsd.logging.log

class LsdFeignInterceptor(private val feignResponseCaptor: FeignResponseCaptor) :
    ResponseInterceptor {

    private val startTime = ThreadLocal.withInitial { 0L }

    override fun aroundDecode(invocationContext: InvocationContext): Any {
        val elapsedTime = System.currentTimeMillis() - startTime.get()
        val response = invocationContext.response()
        val body = if (response.body() != null) print(response.body().asInputStream()) else null
        val convertedResponse = resetBodyData(response, body?.toByteArray()) ?: response
        try {
            feignResponseCaptor.captureResponseInteraction(convertedResponse, body, elapsedTime)
        } catch (t: Throwable) {
            log().error(t.message, t)
            return convertedResponse
        }
        return invocationContext.proceed()
    }

    private fun resetBodyData(response: Response, bodyData: ByteArray?) =
        response.toBuilder().body(bodyData).build()
}
