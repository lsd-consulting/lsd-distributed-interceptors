package com.integreety.yatspec.e2e.captor.trace;

import brave.Tracer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.stream.Stream;

import static java.util.Arrays.stream;
import static java.util.Optional.empty;

@Slf4j
@RequiredArgsConstructor
public class TraceIdRetriever {

    private final Tracer tracer;

    public String getTraceId(final Map<String, Collection<String>> headers) {
        log.info("headers received={}", headers);
        final Collection<String> b3Header = headers.get("b3");
        final Collection<String> xRequestInfo = headers.get("X-Request-Info");
        return retrieveTraceId(b3Header, xRequestInfo);
    }

    private String retrieveTraceId(final Collection<String> b3Header, final Collection<String> xRequestInfo) {
        Optional<String> traceIdOptional = empty();
        if (b3Header != null && !b3Header.isEmpty()) {
            traceIdOptional = getTraceIdFromB3Header(new ArrayList<>(b3Header));
        }

        if (traceIdOptional.isEmpty() && xRequestInfo != null && !xRequestInfo.isEmpty()) {
            traceIdOptional = getTraceIdFromXRequestInfo(new ArrayList<>(xRequestInfo));
        }

        final String traceId = traceIdOptional.orElseGet(this::getTraceIdFromTracer);
        log.info("traceId retrieved={}", traceId);
        return traceId;

    }

    /*
     * The advantage of this approach is that it will create a new traceId and hopefully pass it on with the next request.
     */
    private String getTraceIdFromTracer() {
        return (tracer.currentSpan() == null) ? tracer.nextSpan().context().traceIdString() : tracer.currentSpan().context().traceIdString();
    }

    private Optional<String> getTraceIdFromXRequestInfo(final List<String> xRequestInfo) {
        final String[] split = xRequestInfo.get(0).split(";");
        final String referenceId = stream(split).map(String::trim).filter(x -> x.startsWith("referenceId")).findFirst().orElse("");
        return stream(referenceId.split("=")).skip(1).findAny();
    }

    private Optional<String> getTraceIdFromB3Header(final List<String> b3Header) {
        return Stream.of(b3Header.get(0).split("-")).findFirst();
    }
}
