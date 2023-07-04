package io.lsdconsulting.lsd.distributed.interceptor.interceptor

import feign.Logger.JavaLogger
import feign.Request
import feign.Response
import io.lsdconsulting.lsd.distributed.connector.model.InterceptedInteraction
import io.lsdconsulting.lsd.distributed.interceptor.captor.FeignRequestCaptor
import io.lsdconsulting.lsd.distributed.interceptor.captor.FeignResponseCaptor
import io.lsdconsulting.lsd.distributed.interceptor.config.log
import java.io.IOException

class LsdFeignLoggerInterceptor(private val feignRequestCaptor: FeignRequestCaptor, private val feignResponseCaptor: FeignResponseCaptor) :
    JavaLogger(LsdFeignLoggerInterceptor::class.java) {

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
        super.logAndRebufferResponse(configKey, logLevel, response, elapsedTime)
        val data: InterceptedInteraction
        try {
            data = feignResponseCaptor.captureResponseInteraction(response, elapsedTime)
        } catch (t: Throwable) {
            log().error(t.message, t)
            return response
        }
        return if (data.body != null) resetBodyData(response, data.body!!.toByteArray()) else response
    }

    private fun resetBodyData(response: Response, bodyData: ByteArray) =
        response.toBuilder().body(bodyData).build()
}
