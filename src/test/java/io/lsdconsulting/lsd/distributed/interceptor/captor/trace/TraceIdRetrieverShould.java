package io.lsdconsulting.lsd.distributed.interceptor.captor.trace;

import brave.Span;
import brave.Tracer;
import brave.propagation.TraceContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.apache.commons.lang3.RandomStringUtils.randomAlphabetic;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

class TraceIdRetrieverShould {

    private final Tracer tracer = mock(Tracer.class);
    private final Span span = mock(Span.class);
    private final TraceContext context = mock(TraceContext.class);
    private final TraceIdRetriever underTest = new TraceIdRetriever(tracer);

    private final String traceId = randomAlphabetic(10);

    @Test
    public void retrieveTraceIdFromB3Header() {
        final List<String> xRequestInfoValue = List.of(traceId + "-" + traceId + "-1");
        final Map<String, Collection<String>> headers = Map.of("b3", xRequestInfoValue);

        final String result = underTest.getTraceId(headers);
        assertThat(result, is(traceId));
    }

    @Test
    public void retrieveTraceIdFromXRequestInfoHeader() {
        final List<String> xRequestInfoValue = List.of("referenceId=" + traceId + ";");
        final Map<String, Collection<String>> headers = Map.of("X-Request-Info", xRequestInfoValue);

        final String result = underTest.getTraceId(headers);

        assertThat(result, is(traceId));
    }

    @ParameterizedTest
    @CsvSource(value = {
            "referenceId=123456",
            "something=654321;referenceId=123456"
    })
    public void allowFlexibleFormattingOfXRequestInfoHeaderValue(final String headerValue) {
        final Map<String, Collection<String>> headers = Map.of("X-Request-Info", List.of(headerValue));

        final String result = underTest.getTraceId(headers);

        assertThat(result, is("123456"));
    }

    @ParameterizedTest
    @CsvSource(value = {
            "reference=123456;",
            "reference:123456;"
    })
    public void fallBackToTracerForWrongXRequestInfoHeaderValue(final String headerValue) {
        given(tracer.currentSpan()).willReturn(span);
        given(span.context()).willReturn(context);
        given(context.traceIdString()).willReturn("123456");
        final Map<String, Collection<String>> headers = Map.of("X-Request-Info", List.of(headerValue));

        final String result = underTest.getTraceId(headers);

        assertThat(result, is("123456"));
    }

    @Test
    public void retrieveTraceIdFromTracerCurrentSpan() {
        given(tracer.currentSpan()).willReturn(span);
        given(span.context()).willReturn(context);
        given(context.traceIdString()).willReturn(traceId);

        final String result = underTest.getTraceId(new HashMap<>());

        assertThat(result, is(traceId));
    }

    @Test
    public void retrieveTraceIdFromTracerNextSpan() {
        given(tracer.nextSpan()).willReturn(span);
        given(span.context()).willReturn(context);
        given(context.traceIdString()).willReturn(traceId);

        final String result = underTest.getTraceId(new HashMap<>());

        assertThat(result, is(traceId));
    }
}