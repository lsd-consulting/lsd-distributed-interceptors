package com.integreety.yatspec.e2e.captor.trace;

import brave.Tracer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import static java.util.Arrays.stream;

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
        final String traceId = getTraceIdFromB3Header(b3Header)
                .orElseGet(() -> getTraceIdFromXRequestInfo(xRequestInfo)
                .orElseGet(this::getTraceIdFromTracer));

        log.info("traceId retrieved={}", traceId);
        return traceId;

    }

    /*
     * The advantage of this approach is that it will create a new traceId and hopefully pass it on with the next request.
     */
    private String getTraceIdFromTracer() {
        return (tracer.currentSpan() == null) ? tracer.nextSpan().context().traceIdString() : tracer.currentSpan().context().traceIdString();
    }

    private Optional<String> getTraceIdFromXRequestInfo(final Collection<String> xRequestInfo) {
        if (xRequestInfo != null) {
            return xRequestInfo.stream().findFirst().flatMap(header ->
                    Stream.of(header.split(";")).map(String::trim).filter(x -> x.startsWith("referenceId")).findFirst().flatMap(referenceId -> stream(referenceId.split("=")).skip(1).findAny()));
        } else {
            return Optional.empty();
        }
    }

    private Optional<String> getTraceIdFromB3Header(final Collection<String> b3Header) {
        if (b3Header != null) {
            return b3Header.stream().findFirst().flatMap(header -> Stream.of(header.split("-")).findFirst());
        } else {
            return Optional.empty();
        }
    }
}
