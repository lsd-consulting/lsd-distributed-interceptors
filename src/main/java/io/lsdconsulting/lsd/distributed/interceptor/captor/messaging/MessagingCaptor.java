package io.lsdconsulting.lsd.distributed.interceptor.captor.messaging;

import io.lsdconsulting.lsd.distributed.access.model.InterceptedInteraction;
import io.lsdconsulting.lsd.distributed.access.repository.InterceptedDocumentRepository;
import io.lsdconsulting.lsd.distributed.interceptor.captor.convert.TypeConverter;
import io.lsdconsulting.lsd.distributed.interceptor.captor.header.HeaderRetriever;
import io.lsdconsulting.lsd.distributed.interceptor.captor.http.derive.PropertyServiceNameDeriver;
import io.lsdconsulting.lsd.distributed.interceptor.captor.trace.TraceIdRetriever;
import lombok.RequiredArgsConstructor;
import lsd.format.PrettyPrinter;
import org.springframework.messaging.Message;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.Map;

import static io.lsdconsulting.lsd.distributed.access.model.Type.CONSUME;
import static io.lsdconsulting.lsd.distributed.access.model.Type.PUBLISH;
import static io.lsdconsulting.lsd.distributed.interceptor.captor.http.derive.SourceTargetDeriver.SOURCE_NAME_KEY;
import static io.lsdconsulting.lsd.distributed.interceptor.captor.http.derive.SourceTargetDeriver.TARGET_NAME_KEY;
import static java.util.Collections.emptyMap;
import static org.apache.commons.lang3.StringUtils.isBlank;

@RequiredArgsConstructor
public class MessagingCaptor {

    private final InterceptedDocumentRepository interceptedDocumentRepository;
    private final PropertyServiceNameDeriver propertyServiceNameDeriver;
    private final TraceIdRetriever traceIdRetriever;
    private final HeaderRetriever headerRetriever;
    private final String profile;

    public InterceptedInteraction captureConsumeInteraction(final Message<?> message) {
        String typeIdHeader = (String) message.getHeaders().get("__TypeId__");
        Map<String, Collection<String>> headers = headerRetriever.retrieve(message);
        final InterceptedInteraction interceptedInteraction = InterceptedInteraction.builder()
                .traceId(traceIdRetriever.getTraceId(headers))
                .body(PrettyPrinter.prettyPrint(TypeConverter.convert((byte[])message.getPayload())))
                .requestHeaders(headers)
                .responseHeaders(emptyMap())
                .serviceName(propertyServiceNameDeriver.getServiceName())
                .target(getSource(typeIdHeader))
                .path(propertyServiceNameDeriver.getServiceName())
                .type(CONSUME)
                .profile(profile)
                .createdAt(ZonedDateTime.now(ZoneId.of("UTC")))
                .build();

        interceptedDocumentRepository.save(interceptedInteraction);
        return interceptedInteraction;
    }

    private String getSource(String typeIdHeader) {
        String source = "UNKNOWN";
        if (!isBlank(typeIdHeader)) {
            String[] sourceTokens = typeIdHeader.split("\\.");
            source = sourceTokens[sourceTokens.length - 1];
        }
        return source;
    }

    public InterceptedInteraction capturePublishInteraction(final Message<?> message) {
        String source = (String) message.getHeaders().get(SOURCE_NAME_KEY);
        String target = (String) message.getHeaders().get(TARGET_NAME_KEY);

        Map<String, Collection<String>> headers = headerRetriever.retrieve(message);
        final InterceptedInteraction interceptedInteraction = InterceptedInteraction.builder()
                .traceId(traceIdRetriever.getTraceId(headers))
                .body(TypeConverter.convert((byte[])message.getPayload()))
                .requestHeaders(headers)
                .responseHeaders(emptyMap())
                .serviceName(source != null ? source : propertyServiceNameDeriver.getServiceName())
                .target(target)
                .path(target)
                .type(PUBLISH)
                .profile(profile)
                .createdAt(ZonedDateTime.now(ZoneId.of("UTC")))
                .build();

        interceptedDocumentRepository.save(interceptedInteraction);
        return interceptedInteraction;
    }
}
