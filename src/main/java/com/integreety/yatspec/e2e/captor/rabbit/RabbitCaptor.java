package com.integreety.yatspec.e2e.captor.rabbit;

import com.integreety.yatspec.e2e.captor.http.mapper.PropertyServiceNameDeriver;
import com.integreety.yatspec.e2e.captor.rabbit.header.HeaderRetriever;
import com.integreety.yatspec.e2e.captor.repository.InterceptedDocumentRepository;
import com.integreety.yatspec.e2e.captor.repository.model.InterceptedInteraction;
import com.integreety.yatspec.e2e.captor.repository.model.InterceptedInteractionFactory;
import com.integreety.yatspec.e2e.captor.repository.model.Type;
import com.integreety.yatspec.e2e.captor.trace.TraceIdRetriever;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;

import java.util.Collection;
import java.util.Map;

@RequiredArgsConstructor
public class RabbitCaptor {

    private final InterceptedDocumentRepository interceptedDocumentRepository;
    private final InterceptedInteractionFactory interceptedInteractionFactory;
    private final PropertyServiceNameDeriver propertyServiceNameDeriver;
    private final HeaderRetriever headerRetriever;
    private final TraceIdRetriever traceIdRetriever;

    public void captureInteraction(final String exchange, final Message message, final Type type) {
        final MessageProperties messageProperties = message.getMessageProperties();
        final Map<String, Collection<String>> headers = headerRetriever.retrieve(messageProperties);
        final String traceId = traceIdRetriever.getTraceId(headers);
        final String service = propertyServiceNameDeriver.getServiceName();
        final String body = message.getBody() != null ? new String(message.getBody()) : null;
        final InterceptedInteraction interceptedInteraction = interceptedInteractionFactory.buildFrom(body, traceId, headers, service, exchange, exchange, type);
        interceptedDocumentRepository.save(interceptedInteraction);
    }
}