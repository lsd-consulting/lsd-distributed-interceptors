package io.lsdconsulting.lsd.distributed.interceptor.captor.http;

import feign.Response;
import io.lsdconsulting.lsd.distributed.access.model.InterceptedInteraction;
import io.lsdconsulting.lsd.distributed.interceptor.captor.convert.TypeConverter;
import io.lsdconsulting.lsd.distributed.interceptor.captor.http.derive.HttpStatusDeriver;
import io.lsdconsulting.lsd.distributed.interceptor.captor.http.derive.PathDeriver;
import io.lsdconsulting.lsd.distributed.interceptor.captor.http.derive.SourceTargetDeriver;
import io.lsdconsulting.lsd.distributed.interceptor.captor.trace.TraceIdRetriever;
import io.lsdconsulting.lsd.distributed.interceptor.persistance.RepositoryService;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpResponse;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.Map;

import static io.lsdconsulting.lsd.distributed.access.model.InteractionType.RESPONSE;
import static org.apache.commons.lang3.StringUtils.EMPTY;

@RequiredArgsConstructor
@Slf4j
public class ResponseCaptor {

    private final RepositoryService repositoryService;
    private final SourceTargetDeriver sourceTargetDeriver;
    private final PathDeriver pathDeriver;
    private final TraceIdRetriever traceIdRetriever;
    private final HttpHeaderRetriever httpHeaderRetriever;
    private final HttpStatusDeriver httpStatusDeriver;
    private final String profile;

    @SneakyThrows
    public InterceptedInteraction captureResponseInteraction(final Response response, Long elapsedTime) {
        final var requestHeaders = httpHeaderRetriever.retrieve(response.request());
        final var responseHeaders = httpHeaderRetriever.retrieve(response);
        final String path = pathDeriver.derivePathFrom(response.request().url());
        final String target = sourceTargetDeriver.deriveTarget(requestHeaders, path);
        final String serviceName = sourceTargetDeriver.deriveServiceName(requestHeaders);
        final String traceId = traceIdRetriever.getTraceId(requestHeaders);
        final String httpStatus = httpStatusDeriver.derive(response.status());

        final InterceptedInteraction interceptedInteraction = buildInterceptedInteraction(target, path, traceId, elapsedTime, requestHeaders, responseHeaders, serviceName, TypeConverter.convert(response.body()), httpStatus);
        repositoryService.enqueue(interceptedInteraction);
        return interceptedInteraction;
    }

    public InterceptedInteraction captureResponseInteraction(final HttpRequest request, final ClientHttpResponse response, final String target, final String path, final String traceId, Long elapsedTime) throws IOException {
        final var requestHeaders = httpHeaderRetriever.retrieve(request);
        final var responseHeaders = httpHeaderRetriever.retrieve(response);
        final String serviceName = sourceTargetDeriver.deriveServiceName(requestHeaders);
        final String body = copyBodyToString(response);
        final String httpStatus = response.getStatusCode().toString();

        final InterceptedInteraction interceptedInteraction = buildInterceptedInteraction(target, path, traceId, elapsedTime, requestHeaders, responseHeaders, serviceName, body, httpStatus);
        repositoryService.enqueue(interceptedInteraction);
        return interceptedInteraction;
    }

    private InterceptedInteraction buildInterceptedInteraction(String target, String path, String traceId, Long elapsedTime, Map<String, Collection<String>> requestHeaders, Map<String, Collection<String>> responseHeaders, String serviceName, String body, String httpStatus) {
        return InterceptedInteraction.builder()
                .traceId(traceId)
                .body(body)
                .requestHeaders(requestHeaders)
                .responseHeaders(responseHeaders)
                .serviceName(serviceName)
                .target(target)
                .path(path)
                .httpStatus(httpStatus)
                .interactionType(RESPONSE)
                .profile(profile)
                .elapsedTime(elapsedTime)
                .createdAt(ZonedDateTime.now(ZoneId.of("UTC")))
                .build();
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