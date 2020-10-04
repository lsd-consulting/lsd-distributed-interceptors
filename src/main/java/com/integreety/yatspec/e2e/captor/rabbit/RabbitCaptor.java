package com.integreety.yatspec.e2e.captor.rabbit;

import com.integreety.yatspec.e2e.captor.http.mapper.PropertyServiceNameDeriver;
import com.integreety.yatspec.e2e.captor.rabbit.header.HeaderRetriever;
import com.integreety.yatspec.e2e.captor.repository.InterceptedDocumentRepository;
import com.integreety.yatspec.e2e.captor.repository.model.InterceptedCall;
import com.integreety.yatspec.e2e.captor.repository.model.InterceptedCallFactory;
import com.integreety.yatspec.e2e.captor.repository.model.Type;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;

import java.util.Collection;
import java.util.Map;

@RequiredArgsConstructor
public class RabbitCaptor {

    private final InterceptedDocumentRepository interceptedDocumentRepository;
    private final InterceptedCallFactory interceptedCallFactory;
    private final PropertyServiceNameDeriver propertyServiceNameDeriver;
    private final HeaderRetriever headerRetriever;

    public void captureInteraction(final String exchange, final Message message, final Type type) {
        final MessageProperties messageProperties = message.getMessageProperties();
        final Map<String, Collection<String>> headers = headerRetriever.retrieve(messageProperties);
        final String service = propertyServiceNameDeriver.getServiceName();
        final String body = message.getBody() != null ? new String(message.getBody()) : null;
        final InterceptedCall interceptedCall = interceptedCallFactory.buildFrom(body, headers, service, exchange, type);
        interceptedDocumentRepository.save(interceptedCall);
    }
}