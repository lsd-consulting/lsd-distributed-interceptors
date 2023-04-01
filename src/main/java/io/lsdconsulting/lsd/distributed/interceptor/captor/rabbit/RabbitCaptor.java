package io.lsdconsulting.lsd.distributed.interceptor.captor.rabbit;

import io.lsdconsulting.lsd.distributed.access.model.InteractionType;
import io.lsdconsulting.lsd.distributed.access.model.InterceptedInteraction;
import io.lsdconsulting.lsd.distributed.interceptor.captor.common.PropertyServiceNameDeriver;
import io.lsdconsulting.lsd.distributed.interceptor.captor.convert.TypeConverter;
import io.lsdconsulting.lsd.distributed.interceptor.captor.trace.TraceIdRetriever;
import io.lsdconsulting.lsd.distributed.interceptor.persistance.QueueService;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.Message;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.Map;

import static java.util.Collections.emptyMap;

@RequiredArgsConstructor
public class RabbitCaptor {

    private final QueueService queueService;
    private final PropertyServiceNameDeriver propertyServiceNameDeriver;
    private final TraceIdRetriever traceIdRetriever;
    private final AmqpHeaderRetriever amqpHeaderRetriever;
    private final String profile;

    public InterceptedInteraction captureInteraction(final String exchange, final Message message, final InteractionType type) {

        Map<String, Collection<String>> headers = amqpHeaderRetriever.retrieve(message);
        final InterceptedInteraction interceptedInteraction = InterceptedInteraction.builder()
                .traceId(traceIdRetriever.getTraceId(headers))
                .body(TypeConverter.convert(message.getBody()))
                .requestHeaders(headers)
                .responseHeaders(emptyMap())
                .serviceName(propertyServiceNameDeriver.getServiceName())
                .target(exchange)
                .path(exchange)
                .interactionType(type)
                .profile(profile)
                .createdAt(ZonedDateTime.now(ZoneId.of("UTC")))
                .build();

        queueService.enqueue(interceptedInteraction);
        return interceptedInteraction;
    }
}
