package com.integreety.yatspec.e2e.captor.http;

import com.integreety.yatspec.e2e.captor.http.mapper.PropertyServiceNameDeriver;
import com.integreety.yatspec.e2e.captor.repository.InterceptedDocumentRepository;
import com.integreety.yatspec.e2e.captor.repository.model.InterceptedInteraction;
import com.integreety.yatspec.e2e.captor.repository.model.InterceptedInteractionFactory;
import com.integreety.yatspec.e2e.captor.trace.TraceIdRetriever;
import feign.Response;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpResponse;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import static com.integreety.yatspec.e2e.captor.repository.model.Type.RESPONSE;
import static feign.Util.toByteArray;
import static java.util.stream.Collectors.toMap;
import static org.apache.commons.lang3.StringUtils.EMPTY;

@RequiredArgsConstructor
@Slf4j
public class ResponseCaptor extends PathDerivingCaptor {

    private final InterceptedDocumentRepository interceptedDocumentRepository;
    private final InterceptedInteractionFactory interceptedInteractionFactory;
    private final PropertyServiceNameDeriver propertyServiceNameDeriver;
    private final TraceIdRetriever traceIdRetriever;

    @SneakyThrows
    public InterceptedInteraction captureResponseInteraction(final Response response) {
        try {
            final Map<String, Collection<String>> headers = response.request().headers();
            final String path = derivePath(response.request().url());
            final String target = headerExists(headers, TARGET_NAME_KEY) ? findHeader(headers, TARGET_NAME_KEY).orElse(derivePath(response.request().url())) : UNKNOWN_TARGET;
            final String serviceName = headerExists(headers, SOURCE_NAME_KEY) ? findHeader(headers, SOURCE_NAME_KEY).orElse(propertyServiceNameDeriver.getServiceName()) : propertyServiceNameDeriver.getServiceName();
            final String traceId = traceIdRetriever.getTraceId(response.headers());
            final InterceptedInteraction interceptedInteraction = interceptedInteractionFactory.buildFrom(extractResponseBodyToString(response), headers, response.headers(), traceId, serviceName, target, path, deriveStatus(response.status()), null, RESPONSE);
            interceptedDocumentRepository.save(interceptedInteraction);
            return interceptedInteraction;
        } catch (final RuntimeException e) {
            log.error(e.getMessage(), e);
            throw e;
        }
    }

    public InterceptedInteraction captureResponseInteraction(final HttpRequest request, final ClientHttpResponse response, final String target, final String path, final String traceId) throws IOException {
        final var requestHeaders = standardiseHeaders(request.getHeaders());
        final var responseHeaders = standardiseHeaders(response.getHeaders());
        final String serviceName = headerExists(requestHeaders, SOURCE_NAME_KEY) ? findHeader(requestHeaders, SOURCE_NAME_KEY).orElse(propertyServiceNameDeriver.getServiceName()) : propertyServiceNameDeriver.getServiceName();
        final String body = copyBodyToString(response);
        final InterceptedInteraction interceptedInteraction = interceptedInteractionFactory.buildFrom(body, responseHeaders, requestHeaders, traceId, serviceName, target, path, response.getStatusCode().toString(), null, RESPONSE);
        interceptedDocumentRepository.save(interceptedInteraction);
        return interceptedInteraction;
    }

    private Map<String, Collection<String>> standardiseHeaders(final HttpHeaders headers) {
        return headers.entrySet().stream()
                .collect(toMap(Entry::getKey, Entry::getValue));
    }

    private String extractResponseBodyToString(final Response response) throws IOException {
        return response.body() != null ? new String(toByteArray(response.body().asInputStream())) : null;
    }

    private String deriveStatus(final int code) {
        final Optional<HttpStatus> httpStatus = Optional.ofNullable(HttpStatus.resolve(code));
        return httpStatus.map(HttpStatus::toString)
                .orElse(String.format("<unresolved status:%s>", code));
    }

    private String copyBodyToString(final ClientHttpResponse response) throws IOException {
        if (response.getHeaders().getContentLength() == 0) {
            return EMPTY;
        }
        final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        final InputStream inputStream = response.getBody();
        inputStream.transferTo(outputStream);
        return outputStream.toString();
    }
}