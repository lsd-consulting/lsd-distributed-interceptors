package io.lsdconsulting.lsd.distributed.interceptor;

import io.lsdconsulting.lsd.distributed.captor.http.RequestCaptor;
import io.lsdconsulting.lsd.distributed.captor.http.ResponseCaptor;
import io.lsdconsulting.lsd.distributed.captor.repository.model.InterceptedInteraction;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;

/**
 * Created to intercept rest template calls for LSD interactions.
 * Attempts to reset the input stream so that no data is lost on reading the response body
 */
@Slf4j
@RequiredArgsConstructor
public class LsdRestTemplateInterceptor implements ClientHttpRequestInterceptor {

    private final RequestCaptor requestCaptor;
    private final ResponseCaptor responseCaptor;

    @Override
    public ClientHttpResponse intercept(final HttpRequest request, final byte[] body, final ClientHttpRequestExecution execution) throws IOException {
        final InterceptedInteraction interceptedInteraction;
        try {
            interceptedInteraction = requestCaptor.captureRequestInteraction(request, new String(body));
        } catch (final Throwable t) {
            log.error(t.getMessage(), t);
            return execution.execute(request, body);
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