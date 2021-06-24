package io.lsdconsulting.lsd.distributed.captor.http;

import feign.Response;
import io.lsdconsulting.lsd.distributed.captor.convert.TypeConverter;
import io.lsdconsulting.lsd.distributed.captor.http.derive.PathDeriver;
import io.lsdconsulting.lsd.distributed.captor.http.derive.SourceTargetDeriver;
import io.lsdconsulting.lsd.distributed.captor.repository.InterceptedDocumentRepository;
import io.lsdconsulting.lsd.distributed.captor.repository.model.InterceptedInteraction;
import io.lsdconsulting.lsd.distributed.captor.repository.model.InterceptedInteractionFactory;
import io.lsdconsulting.lsd.distributed.captor.repository.model.Type;
import io.lsdconsulting.lsd.distributed.captor.trace.TraceIdRetriever;
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

import static java.util.stream.Collectors.toMap;
import static org.apache.commons.lang3.StringUtils.EMPTY;

@RequiredArgsConstructor
@Slf4j
public class ResponseCaptor {

    private final InterceptedDocumentRepository interceptedDocumentRepository;
    private final InterceptedInteractionFactory interceptedInteractionFactory;
    private final SourceTargetDeriver sourceTargetDeriver;
    private final PathDeriver pathDeriver;
    private final TraceIdRetriever traceIdRetriever;

    @SneakyThrows
    public InterceptedInteraction captureResponseInteraction(final Response response) {
        final Map<String, Collection<String>> requestHeaders = response.request().headers();
        final String path = pathDeriver.derivePathFrom(response.request().url());
        final String target = sourceTargetDeriver.deriveTarget(requestHeaders, path);
        final String serviceName = sourceTargetDeriver.deriveServiceName(requestHeaders);
        final String traceId = traceIdRetriever.getTraceId(requestHeaders);
        final InterceptedInteraction interceptedInteraction = interceptedInteractionFactory.buildFrom(TypeConverter.convert(response.body()), requestHeaders, response.headers(), traceId, serviceName, target, path, deriveStatus(response.status()), null, Type.RESPONSE);
        interceptedDocumentRepository.save(interceptedInteraction);
        return interceptedInteraction;
    }


    public InterceptedInteraction captureResponseInteraction(final HttpRequest request, final ClientHttpResponse response, final String target, final String path, final String traceId) throws IOException {
        final var requestHeaders = standardiseHeaders(request.getHeaders());
        final var responseHeaders = standardiseHeaders(response.getHeaders());
        final String serviceName = sourceTargetDeriver.deriveServiceName(requestHeaders);
        final String body = copyBodyToString(response);
        final InterceptedInteraction interceptedInteraction = interceptedInteractionFactory.buildFrom(body, requestHeaders, responseHeaders, traceId, serviceName, target, path, response.getStatusCode().toString(), null, Type.RESPONSE);
        interceptedDocumentRepository.save(interceptedInteraction);
        return interceptedInteraction;
    }

    private Map<String, Collection<String>> standardiseHeaders(final HttpHeaders headers) {
        return headers.entrySet().stream()
                .collect(toMap(Entry::getKey, Entry::getValue));
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