package io.lsdconsulting.lsd.distributed.interceptor.interceptor

import feign.Logger.JavaLogger
import feign.Request
import feign.Response
import io.lsdconsulting.lsd.distributed.access.model.InterceptedInteraction
import io.lsdconsulting.lsd.distributed.interceptor.captor.http.RequestCaptor
import io.lsdconsulting.lsd.distributed.interceptor.captor.http.ResponseCaptor
import io.lsdconsulting.lsd.distributed.interceptor.config.log
import java.io.IOException

class LsdFeignLoggerInterceptor(private val requestCaptor: RequestCaptor, private val responseCaptor: ResponseCaptor) :
    JavaLogger(LsdFeignLoggerInterceptor::class.java) {

    public override fun logRequest(configKey: String, level: Level, request: Request) {
        super.logRequest(configKey, level, request)
        try {
            requestCaptor.captureRequestInteraction(request)
        } catch (t: Throwable) {
            log().error(t.message, t)
        }
    }

    @Throws(IOException::class)
    public override fun logAndRebufferResponse(configKey: String, logLevel: Level, response: Response, elapsedTime: Long): Response {
        super.logAndRebufferResponse(configKey, logLevel, response, elapsedTime)
        val data: InterceptedInteraction
        try {
            data = responseCaptor.captureResponseInteraction(response, elapsedTime)
        } catch (t: Throwable) {
            log().error(t.message, t)
            return response
        }
        return if (data.body != null) resetBodyData(response, data.body!!.toByteArray()) else response
    }

    private fun resetBodyData(response: Response, bodyData: ByteArray) =
        response.toBuilder().body(bodyData).build()
}
