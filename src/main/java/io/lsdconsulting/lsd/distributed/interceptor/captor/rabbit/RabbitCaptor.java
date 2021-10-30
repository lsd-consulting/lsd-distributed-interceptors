package io.lsdconsulting.lsd.distributed.interceptor.captor.rabbit;

import io.lsdconsulting.lsd.distributed.access.model.InterceptedInteraction;
import io.lsdconsulting.lsd.distributed.access.model.Type;
import io.lsdconsulting.lsd.distributed.access.repository.InterceptedDocumentRepository;
import io.lsdconsulting.lsd.distributed.interceptor.captor.convert.TypeConverter;
import io.lsdconsulting.lsd.distributed.interceptor.captor.header.HeaderRetriever;
import io.lsdconsulting.lsd.distributed.interceptor.captor.http.derive.PropertyServiceNameDeriver;
import io.lsdconsulting.lsd.distributed.interceptor.captor.trace.TraceIdRetriever;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.Message;

import java.time.ZoneId;
import java.time.ZonedDateTime;

import static java.util.Collections.emptyMap;

@RequiredArgsConstructor
public class RabbitCaptor {

    private final InterceptedDocumentRepository interceptedDocumentRepository;
    private final PropertyServiceNameDeriver propertyServiceNameDeriver;
    private final TraceIdRetriever traceIdRetriever;
    private final HeaderRetriever headerRetriever;
    private final String profile;

    public InterceptedInteraction captureInteraction(final String exchange, final Message message, final Type type) {

        final InterceptedInteraction interceptedInteraction = InterceptedInteraction.builder()
                .traceId(traceIdRetriever.getTraceId(headerRetriever.retrieve(message)))
                .body(TypeConverter.convert(message.getBody()))
                .requestHeaders(headerRetriever.retrieve(message))
                .responseHeaders(emptyMap())
                .serviceName(propertyServiceNameDeriver.getServiceName())
                .target(exchange)
                .path(exchange)
                .type(type)
                .profile(profile)
                .createdAt(ZonedDateTime.now(ZoneId.of("UTC")))
                .build();

        interceptedDocumentRepository.save(interceptedInteraction);
        return interceptedInteraction;
    }
}
