package io.lsdconsulting.lsd.distributed.interceptor.captor.rabbit;

import io.lsdconsulting.lsd.distributed.access.model.InteractionType;
import io.lsdconsulting.lsd.distributed.access.model.InterceptedInteraction;
import io.lsdconsulting.lsd.distributed.interceptor.captor.common.PropertyServiceNameDeriver;
import io.lsdconsulting.lsd.distributed.interceptor.captor.convert.TypeConverter;
import io.lsdconsulting.lsd.distributed.interceptor.captor.trace.TraceIdRetriever;
import io.lsdconsulting.lsd.distributed.interceptor.persistance.RepositoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.Message;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.Map;

import static java.util.Collections.emptyMap;

@RequiredArgsConstructor
public class RabbitCaptor {

    private final RepositoryService repositoryService;
    private final PropertyServiceNameDeriver propertyServiceNameDeriver;
    private final TraceIdRetriever traceIdRetriever;
    private final AmqpHeaderRetriever amqpHeaderRetriever;
    private final String profile;

    public InterceptedInteraction captureInteraction(final String exchange, final Message message, final InteractionType type) {

        Map<String, Collection<String>> headers = amqpHeaderRetriever.retrieve(message);
        final InterceptedInteraction interceptedInteraction = new InterceptedInteraction(
                traceIdRetriever.getTraceId(headers),
                TypeConverter.convert(message.getBody()),
                headers,
                emptyMap(),
                propertyServiceNameDeriver.getServiceName(),
                exchange,
                exchange,
                null,
                null,
                type,
                profile,
                0L,
                ZonedDateTime.now(ZoneId.of("UTC")));

        repositoryService.enqueue(interceptedInteraction);
        return interceptedInteraction;
    }
}
