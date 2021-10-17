package io.lsdconsulting.lsd.distributed.interceptor.interceptor;

import feign.Logger;
import feign.Request;
import feign.Response;
import io.lsdconsulting.lsd.distributed.access.model.InterceptedInteraction;
import io.lsdconsulting.lsd.distributed.interceptor.captor.http.RequestCaptor;
import io.lsdconsulting.lsd.distributed.interceptor.captor.http.ResponseCaptor;
import org.apache.commons.lang3.RandomUtils;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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
    private final Long elapsedTime = RandomUtils.nextLong();

    @Test
    void logsRequest() {
        underTest.logRequest("configKey", level, request);

        verify(requestCaptor).captureRequestInteraction(request);
    }
   @Test
    void logAndRebufferResponse() throws IOException {
        given(responseCaptor.captureResponseInteraction(any(), eq(elapsedTime))).willReturn(InterceptedInteraction.builder().build());

        underTest.logAndRebufferResponse("configKey", level, response, elapsedTime);

        verify(responseCaptor).captureResponseInteraction(response, elapsedTime);
    }
}
