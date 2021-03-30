package com.integreety.yatspec.e2e.interceptor;

import com.integreety.yatspec.e2e.captor.http.RequestCaptor;
import com.integreety.yatspec.e2e.captor.http.ResponseCaptor;
import com.integreety.yatspec.e2e.captor.repository.model.InterceptedInteraction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static wiremock.org.apache.commons.lang3.RandomStringUtils.randomAlphabetic;

class LsdRestTemplateInterceptorShould {


    private final RequestCaptor requestCaptor = mock(RequestCaptor.class);
    private final ResponseCaptor responseCaptor = mock(ResponseCaptor.class);
    private final HttpRequest httpRequest = mock(HttpRequest.class);
    private final ClientHttpResponse httpResponse = mock(ClientHttpResponse.class);
    private final ClientHttpRequestExecution execution = mock(ClientHttpRequestExecution.class);

    private final LsdRestTemplateInterceptor underTest = new LsdRestTemplateInterceptor(requestCaptor, responseCaptor);

    private final String body = randomAlphabetic(20);
    private final String target = randomAlphabetic(20);
    private final String path = randomAlphabetic(20);
    private final String traceId = randomAlphabetic(20);

    @BeforeEach
    public void setup() throws IOException {
        given(requestCaptor.captureRequestInteraction(any(), eq(body))).willReturn(InterceptedInteraction.builder().target(target).path(path).traceId(traceId).build());
        when(execution.execute(any(), any())).thenReturn(httpResponse);
    }

    @Test
    void passActualRequestToExecutor() throws IOException {
        given(requestCaptor.captureRequestInteraction(any(), eq(body))).willReturn(InterceptedInteraction.builder().build());

        underTest.intercept(httpRequest, body.getBytes(), execution);

        verify(execution).execute(httpRequest, body.getBytes());
    }


    @Test
    void returnsActualResponse() throws IOException {
        given(requestCaptor.captureRequestInteraction(any(), eq(body))).willReturn(InterceptedInteraction.builder().build());
        final ClientHttpResponse interceptedResponse = underTest.intercept(httpRequest, body.getBytes(), execution);
        assertThat(interceptedResponse, is(httpResponse));
    }

    @Test
    void logRequestInteraction() throws IOException {
        given(requestCaptor.captureRequestInteraction(any(), eq(body))).willReturn(InterceptedInteraction.builder().build());
        underTest.intercept(httpRequest, body.getBytes(), execution);

        verify(requestCaptor).captureRequestInteraction(eq(httpRequest), eq(body));
    }

    @Test
    void logResponseInteraction() throws IOException {
        underTest.intercept(httpRequest, body.getBytes(), execution);

        verify(responseCaptor).captureResponseInteraction(eq(httpRequest), eq(httpResponse), eq(target), eq(path), eq(traceId));
    }
}