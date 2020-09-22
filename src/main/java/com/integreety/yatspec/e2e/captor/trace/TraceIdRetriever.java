package com.integreety.yatspec.e2e.captor.trace;

import brave.Tracer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
public class TraceIdRetriever {

    private final Tracer tracer; // TODO Should this be optional?

    public String getTraceId(final Map<String, Collection<String>> headers) {
        log.info("headers received={}", headers);
        final Collection<String> b3Header = headers.get("b3");
        final Collection<String> xRequestInfo = headers.get("X-Request-Info");
        return retrieveTraceId(b3Header, xRequestInfo);
    }

    private String retrieveTraceId(final Collection<String> b3Header, final Collection<String> xRequestInfo) {
        final String traceId;
        if (b3Header != null && b3Header.size() > 0) {
            traceId = getTraceIdFromB3Header(b3Header);
        } else if (xRequestInfo != null && xRequestInfo.size() > 0) {
            traceId = getTraceIdFromXRequestInfo(xRequestInfo);
        } else {
            traceId = getTraceIdFromTracer();
        }
        log.info("traceId retrieved={}", traceId);
        return traceId;
    }

    private String getTraceIdFromTracer() {
        return (tracer.currentSpan() == null) ? tracer.nextSpan().context().traceIdString() : tracer.currentSpan().context().traceIdString();
    }

    private String getTraceIdFromXRequestInfo(final Collection<String> xRequestInfo) {
        final String[] split = xRequestInfo.stream().findFirst().get().split(";");
        final String referenceId = Arrays.stream(split).map(String::trim).filter(x -> x.startsWith("referenceId")).findFirst().get();
        return referenceId.split("=")[1];
    }

    private String getTraceIdFromB3Header(final Collection<String> b3Header) {
        return Arrays.stream(b3Header.stream().findFirst().get().split("-")).findFirst().get();
    }
}
