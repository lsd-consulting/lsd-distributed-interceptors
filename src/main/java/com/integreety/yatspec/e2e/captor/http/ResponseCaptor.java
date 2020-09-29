package com.integreety.yatspec.e2e.captor.http;

import com.integreety.yatspec.e2e.captor.http.mapper.PropertyServiceNameDeriver;
import com.integreety.yatspec.e2e.captor.repository.InterceptedDocumentRepository;
import com.integreety.yatspec.e2e.captor.repository.model.InterceptedCall;
import com.integreety.yatspec.e2e.captor.repository.model.InterceptedCallFactory;
import feign.Response;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpResponse;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.integreety.yatspec.e2e.captor.repository.model.Type.RESPONSE;
import static feign.Util.toByteArray;
import static org.apache.commons.lang3.StringUtils.EMPTY;

@RequiredArgsConstructor
@Slf4j
public class ResponseCaptor extends PathDerivingCaptor {

    private final InterceptedDocumentRepository interceptedDocumentRepository;
    private final InterceptedCallFactory interceptedCallFactory;
    private final PropertyServiceNameDeriver propertyServiceNameDeriver;

    @SneakyThrows
    public InterceptedCall captureResponseInteraction(final Response response) {
        try {
            final String path = derivePath(response.request().url());
            final String serviceName = propertyServiceNameDeriver.getServiceName();
            final InterceptedCall interceptedCall = interceptedCallFactory.buildFrom(extractResponseBodyToString(response), response.headers(), serviceName, path, deriveStatus(response.status()), null, RESPONSE);
            interceptedDocumentRepository.save(interceptedCall);
            return interceptedCall;
        } catch (final RuntimeException e) {
            log.error(e.getMessage(), e);
            throw e;
        }
    }

    public void captureResponseInteraction(final ClientHttpResponse response, final String path) throws IOException {
        final String body = copyBodyToString(response);
        final String serviceName = propertyServiceNameDeriver.getServiceName();
        final var headers = response.getHeaders().entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> (Collection<String>) e.getValue()));
        final InterceptedCall interceptedCall = interceptedCallFactory.buildFrom(body, headers, serviceName, path, response.getStatusCode().toString(), null, RESPONSE);
        interceptedDocumentRepository.save(interceptedCall);
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