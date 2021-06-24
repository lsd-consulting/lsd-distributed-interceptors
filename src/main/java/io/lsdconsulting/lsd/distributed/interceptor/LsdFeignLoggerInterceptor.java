package io.lsdconsulting.lsd.distributed.interceptor;

import feign.Logger;
import feign.Request;
import feign.Response;
import io.lsdconsulting.lsd.distributed.captor.http.RequestCaptor;
import io.lsdconsulting.lsd.distributed.captor.http.ResponseCaptor;
import io.lsdconsulting.lsd.distributed.captor.repository.model.InterceptedInteraction;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

@Slf4j
public class LsdFeignLoggerInterceptor extends Logger.JavaLogger {

    private final RequestCaptor requestCaptor;
    private final ResponseCaptor responseCaptor;

    public LsdFeignLoggerInterceptor(final RequestCaptor requestCaptor, final ResponseCaptor responseCaptor) {
        super(LsdFeignLoggerInterceptor.class);
        this.requestCaptor = requestCaptor;
        this.responseCaptor = responseCaptor;
    }

    @Override
    protected void logRequest(final String configKey, final Level level, final Request request) {
        super.logRequest(configKey, level, request);
        try {
            requestCaptor.captureRequestInteraction(request);
        } catch (final Throwable t) {
            log.error(t.getMessage(), t);
        }
    }

    @Override
    protected Response logAndRebufferResponse(final String configKey, final Level logLevel, final Response response, final long elapsedTime) throws IOException {
        super.logAndRebufferResponse(configKey, logLevel, response, elapsedTime);

        InterceptedInteraction data = null;
        try {
            data = responseCaptor.captureResponseInteraction(response);
        } catch (final Throwable t) {
            log.error(t.getMessage(), t);
        }

        return data != null && data.getBody() != null ? resetBodyData(response, data.getBody().getBytes()) : response;
    }

    private Response resetBodyData(final Response response, final byte[] bodyData) {
        return response.toBuilder().body(bodyData).build();
    }
}
