package com.integreety.yatspec.e2e.interceptor;

import com.integreety.yatspec.e2e.captor.http.RequestCaptor;
import com.integreety.yatspec.e2e.captor.http.ResponseCaptor;
import com.integreety.yatspec.e2e.captor.repository.model.InterceptedInteraction;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;

/**
 * Created to intercept rest template calls for Yatspec interactions.
 * Attempts to reset the input stream so that no data is lost on reading the response body
 */
@Slf4j
@RequiredArgsConstructor
public class LsdRestTemplateInterceptor implements ClientHttpRequestInterceptor {

    private final RequestCaptor requestCaptor;
    private final ResponseCaptor responseCaptor;

    @Override
    public ClientHttpResponse intercept(final HttpRequest request, final byte[] body, final ClientHttpRequestExecution execution) throws IOException {
        InterceptedInteraction interceptedInteraction = null;
        try {
            interceptedInteraction = requestCaptor.captureRequestInteraction(request, new String(body));
        } catch (final Throwable t) {
            log.error(t.getMessage(), t);
        }
        final ClientHttpResponse response = execution.execute(request, body);
        try {
            responseCaptor.captureResponseInteraction(request, response, interceptedInteraction.getTarget(), interceptedInteraction.getPath(), interceptedInteraction.getTraceId());
        } catch (final Throwable t) {
            log.error(t.getMessage(), t);
        }
        return response;
    }
}