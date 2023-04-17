package io.lsdconsulting.lsd.distributed.interceptor.captor.messaging;

import io.lsdconsulting.lsd.distributed.access.model.InterceptedInteraction;
import io.lsdconsulting.lsd.distributed.interceptor.captor.common.PropertyServiceNameDeriver;
import io.lsdconsulting.lsd.distributed.interceptor.captor.convert.TypeConverter;
import io.lsdconsulting.lsd.distributed.interceptor.captor.trace.TraceIdRetriever;
import io.lsdconsulting.lsd.distributed.interceptor.persistance.RepositoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lsd.format.PrettyPrinter;
import org.springframework.messaging.Message;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.Map;

import static io.lsdconsulting.lsd.distributed.access.model.InteractionType.CONSUME;
import static io.lsdconsulting.lsd.distributed.access.model.InteractionType.PUBLISH;
import static io.lsdconsulting.lsd.distributed.interceptor.captor.http.derive.SourceTargetDeriver.SOURCE_NAME_KEY;
import static io.lsdconsulting.lsd.distributed.interceptor.captor.http.derive.SourceTargetDeriver.TARGET_NAME_KEY;
import static java.util.Collections.emptyMap;
import static org.apache.commons.lang3.StringUtils.isBlank;

@RequiredArgsConstructor
@Slf4j
public class MessagingCaptor {

    private final RepositoryService repositoryService;
    private final PropertyServiceNameDeriver propertyServiceNameDeriver;
    private final TraceIdRetriever traceIdRetriever;
    private final MessagingHeaderRetriever messagingHeaderRetriever;
    private final String profile;

    public InterceptedInteraction captureConsumeInteraction(final Message<?> message) {
        Map<String, Collection<String>> headers = messagingHeaderRetriever.retrieve(message);
        final InterceptedInteraction interceptedInteraction = new InterceptedInteraction(
                traceIdRetriever.getTraceId(headers),
                PrettyPrinter.prettyPrint(TypeConverter.convert((byte[]) message.getPayload())),
                headers,
                emptyMap(),
                propertyServiceNameDeriver.getServiceName(),
                getSource(message),
                propertyServiceNameDeriver.getServiceName(),
                null,
                null,
                CONSUME,
                profile,
                0L,
                ZonedDateTime.now(ZoneId.of("UTC")));

        repositoryService.enqueue(interceptedInteraction);
        return interceptedInteraction;
    }

    private String getSource(Message<?> message) {
        String source = (String) message.getHeaders().get(TARGET_NAME_KEY);
        if (isBlank(source)) {
            String typeIdHeader = (String) message.getHeaders().get("__TypeId__");
            source = getSourceFrom(typeIdHeader);
        }
        log.debug("found source:{}", source);
        return source;
    }

    private String getSourceFrom(String typeIdHeader) {
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

        Map<String, Collection<String>> headers = messagingHeaderRetriever.retrieve(message);
        final InterceptedInteraction interceptedInteraction = new InterceptedInteraction(
                traceIdRetriever.getTraceId(headers),
                TypeConverter.convert((byte[]) message.getPayload()),
                headers,
                emptyMap(),
                source != null ? source : propertyServiceNameDeriver.getServiceName(),
                target,
                target,
                null,
                null,
                PUBLISH,
                profile,
                0L,
                ZonedDateTime.now(ZoneId.of("UTC")));

        repositoryService.enqueue(interceptedInteraction);
        return interceptedInteraction;
    }
}
