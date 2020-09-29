package com.integreety.yatspec.e2e.interceptor;

import com.integreety.yatspec.e2e.captor.http.RequestCaptor;
import com.integreety.yatspec.e2e.captor.http.ResponseCaptor;
import com.integreety.yatspec.e2e.captor.repository.model.InterceptedCall;
import feign.Logger;
import feign.Request;
import feign.Response;
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
        requestCaptor.captureRequestInteraction(request);
    }

    @Override
    protected Response logAndRebufferResponse(final String configKey, final Level logLevel, final Response response, final long elapsedTime) throws IOException {
        super.logAndRebufferResponse(configKey, logLevel, response, elapsedTime);
        final InterceptedCall data = responseCaptor.captureResponseInteraction(response);
        return resetBodyData(response, data.getBody() != null ? data.getBody().getBytes() : null);
    }

    private Response resetBodyData(final Response response, final byte[] bodyData) {
        return response.toBuilder().body(bodyData).build();
    }
}