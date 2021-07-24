package io.lsdconsulting.lsd.distributed.captor.rabbit;

import io.lsdconsulting.lsd.distributed.captor.header.HeaderRetriever;
import io.lsdconsulting.lsd.distributed.captor.http.derive.PropertyServiceNameDeriver;
import io.lsdconsulting.lsd.distributed.captor.repository.InterceptedDocumentRepository;
import io.lsdconsulting.lsd.distributed.captor.repository.model.InterceptedInteraction;
import io.lsdconsulting.lsd.distributed.captor.repository.model.InterceptedInteractionFactory;
import io.lsdconsulting.lsd.distributed.captor.repository.model.Type;
import io.lsdconsulting.lsd.distributed.captor.trace.TraceIdRetriever;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.Message;

import java.util.Collection;
import java.util.Map;

import static io.lsdconsulting.lsd.distributed.captor.convert.TypeConverter.convert;

@RequiredArgsConstructor
public class RabbitCaptor {

    private final InterceptedDocumentRepository interceptedDocumentRepository;
    private final InterceptedInteractionFactory interceptedInteractionFactory;
    private final PropertyServiceNameDeriver propertyServiceNameDeriver;
    private final TraceIdRetriever traceIdRetriever;
    private final HeaderRetriever headerRetriever;

    public InterceptedInteraction captureInteraction(final String exchange, final Message message, final Type type) {
        final Map<String, Collection<String>> headers = headerRetriever.retrieve(message);
        final String traceId = traceIdRetriever.getTraceId(headers);
        final String service = propertyServiceNameDeriver.getServiceName();
        final String body = convert(message.getBody());
        final InterceptedInteraction interceptedInteraction = interceptedInteractionFactory.buildFrom(body, traceId, headers, service, exchange, exchange, type);
        interceptedDocumentRepository.save(interceptedInteraction);
        return interceptedInteraction;
    }
}