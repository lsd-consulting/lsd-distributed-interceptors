package com.integreety.yatspec.e2e.interceptor;

import com.integreety.yatspec.e2e.captor.http.RequestCaptor;
import com.integreety.yatspec.e2e.captor.http.ResponseCaptor;
import com.integreety.yatspec.e2e.captor.repository.model.InterceptedInteraction;
import feign.Logger;
import feign.Request;
import feign.Response;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class LsdFeignLoggerInterceptorShould {

    private final RequestCaptor requestCaptor = mock(RequestCaptor.class);
    private final ResponseCaptor responseCaptor = mock(ResponseCaptor.class);
    private final Request request = mock(Request.class);
    private final Response response = mock(Response.class);

    private final LsdFeignLoggerInterceptor underTest = new LsdFeignLoggerInterceptor(requestCaptor, responseCaptor);

    private final Logger.Level level = Logger.Level.BASIC;

    @Test
    void logsRequest() {
        underTest.logRequest("configKey", level, request);

        verify(requestCaptor).captureRequestInteraction(request);
    }
   @Test
    void logAndRebufferResponse() throws IOException {
        given(responseCaptor.captureResponseInteraction(any())).willReturn(InterceptedInteraction.builder().build());

        underTest.logAndRebufferResponse("configKey", level, response, 0);

        verify(responseCaptor).captureResponseInteraction(response);
    }
}
