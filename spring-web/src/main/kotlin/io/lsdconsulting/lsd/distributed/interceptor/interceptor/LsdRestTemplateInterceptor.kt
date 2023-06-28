package io.lsdconsulting.lsd.distributed.interceptor.interceptor

import io.lsdconsulting.lsd.distributed.connector.model.InterceptedInteraction
import io.lsdconsulting.lsd.distributed.interceptor.captor.http.RequestCaptor
import io.lsdconsulting.lsd.distributed.interceptor.captor.http.ResponseCaptor
import io.lsdconsulting.lsd.distributed.interceptor.config.log
import io.lsdconsulting.lsd.distributed.interceptor.interceptor.TimeHelper.getNow
import org.springframework.http.HttpRequest
import org.springframework.http.client.ClientHttpRequestExecution
import org.springframework.http.client.ClientHttpRequestInterceptor
import org.springframework.http.client.ClientHttpResponse
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 * Created to intercept rest template calls for LSD interactions.
 * Attempts to reset the input stream so that no data is lost on reading the response body
 */
class LsdRestTemplateInterceptor(
    private val requestCaptor: RequestCaptor,
    private val responseCaptor: ResponseCaptor,
) : ClientHttpRequestInterceptor {

    @Throws(IOException::class)
    override fun intercept(
        request: HttpRequest,
        body: ByteArray,
        execution: ClientHttpRequestExecution
    ): ClientHttpResponse {
        val startDateTime = getNow()

        val interceptedInteraction: InterceptedInteraction = try {
            requestCaptor.captureRequestInteraction(request, String(body))
        } catch (t: Throwable) {
            log().error(t.message, t)
            return execution.execute(request, body)
        }

        val response = execution.execute(request, body)

        val elapsedTime = TimeUnit.NANOSECONDS.toMillis(getNow() - startDateTime)

        try {
            responseCaptor.captureResponseInteraction(
                request,
                response,
                interceptedInteraction.target,
                interceptedInteraction.path,
                interceptedInteraction.traceId,
                elapsedTime
            )
        } catch (t: Throwable) {
            log().error(t.message, t)
        }
        return response
    }
}
