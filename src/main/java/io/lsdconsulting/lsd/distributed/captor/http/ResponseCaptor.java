package io.lsdconsulting.lsd.distributed.captor.http;

import feign.Response;
import io.lsdconsulting.lsd.distributed.captor.convert.TypeConverter;
import io.lsdconsulting.lsd.distributed.captor.header.HeaderRetriever;
import io.lsdconsulting.lsd.distributed.captor.http.derive.HttpStatusDeriver;
import io.lsdconsulting.lsd.distributed.captor.http.derive.PathDeriver;
import io.lsdconsulting.lsd.distributed.captor.http.derive.SourceTargetDeriver;
import io.lsdconsulting.lsd.distributed.captor.repository.InterceptedDocumentRepository;
import io.lsdconsulting.lsd.distributed.captor.repository.model.InterceptedInteraction;
import io.lsdconsulting.lsd.distributed.captor.repository.model.InterceptedInteractionFactory;
import io.lsdconsulting.lsd.distributed.captor.trace.TraceIdRetriever;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpResponse;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import static io.lsdconsulting.lsd.distributed.captor.repository.model.Type.RESPONSE;
import static org.apache.commons.lang3.StringUtils.EMPTY;

@RequiredArgsConstructor
@Slf4j
public class ResponseCaptor {

    private final InterceptedDocumentRepository interceptedDocumentRepository;
    private final InterceptedInteractionFactory interceptedInteractionFactory;
    private final SourceTargetDeriver sourceTargetDeriver;
    private final PathDeriver pathDeriver;
    private final TraceIdRetriever traceIdRetriever;
    private final HeaderRetriever headerRetriever;
    private final HttpStatusDeriver httpStatusDeriver;

    @SneakyThrows
    public InterceptedInteraction captureResponseInteraction(final Response response, Long elapsedTime) {
        final var requestHeaders = headerRetriever.retrieve(response.request());
        final var responseHeaders = headerRetriever.retrieve(response);
        final String path = pathDeriver.derivePathFrom(response.request().url());
        final String target = sourceTargetDeriver.deriveTarget(requestHeaders, path);
        final String serviceName = sourceTargetDeriver.deriveServiceName(requestHeaders);
        final String traceId = traceIdRetriever.getTraceId(requestHeaders);
        final String httpStatus = httpStatusDeriver.derive(response.status());
        final InterceptedInteraction interceptedInteraction = interceptedInteractionFactory.buildFrom(TypeConverter.convert(response.body()), requestHeaders, responseHeaders, traceId, serviceName, target, path, httpStatus, elapsedTime, null, RESPONSE);
        interceptedDocumentRepository.save(interceptedInteraction);
        return interceptedInteraction;
    }

    public InterceptedInteraction captureResponseInteraction(final HttpRequest request, final ClientHttpResponse response, final String target, final String path, final String traceId, Long elapsedTime) throws IOException {
        final var requestHeaders = headerRetriever.retrieve(request);
        final var responseHeaders = headerRetriever.retrieve(response);
        final String serviceName = sourceTargetDeriver.deriveServiceName(requestHeaders);
        final String body = copyBodyToString(response);
        final String httpStatus = response.getStatusCode().toString();
        final InterceptedInteraction interceptedInteraction = interceptedInteractionFactory.buildFrom(body, requestHeaders, responseHeaders, traceId, serviceName, target, path, httpStatus, elapsedTime, null, RESPONSE);
        interceptedDocumentRepository.save(interceptedInteraction);
        return interceptedInteraction;
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