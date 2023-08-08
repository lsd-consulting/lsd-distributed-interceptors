package io.lsdconsulting.lsd.distributed.interceptor.interceptor

import feign.Request
import feign.Response
import feign.slf4j.Slf4jLogger
import io.lsdconsulting.lsd.distributed.interceptor.captor.FeignRequestCaptor
import io.lsdconsulting.lsd.distributed.interceptor.captor.FeignResponseCaptor
import io.lsdconsulting.lsd.distributed.interceptor.captor.common.print
import io.lsdconsulting.lsd.distributed.interceptor.config.log
import java.io.IOException

class LsdFeignLoggerInterceptor(private val feignRequestCaptor: FeignRequestCaptor, private val feignResponseCaptor: FeignResponseCaptor) :
    Slf4jLogger(LsdFeignLoggerInterceptor::class.java) {

    public override fun logRequest(configKey: String, level: Level, request: Request) {
        super.logRequest(configKey, level, request)
        try {
            feignRequestCaptor.captureRequestInteraction(request)
        } catch (t: Throwable) {
            log().error(t.message, t)
        }
    }

    @Throws(IOException::class)
    public override fun logAndRebufferResponse(configKey: String, logLevel: Level, response: Response, elapsedTime: Long): Response {
        val body = if (response.body() != null) print(response.body().asInputStream()) else null
        val convertedResponse = resetBodyData(response, body?.toByteArray()) ?: response
        super.logAndRebufferResponse(configKey, logLevel, convertedResponse, elapsedTime)
        try {
            feignResponseCaptor.captureResponseInteraction(convertedResponse, body, elapsedTime)
        } catch (t: Throwable) {
            log().error(t.message, t)
            return convertedResponse
        }
        return convertedResponse
    }

    private fun resetBodyData(response: Response, bodyData: ByteArray?) =
        response.toBuilder().body(bodyData).build()
}
