package com.integreety.yatspec.e2e.captor.rabbit;

import com.integreety.yatspec.e2e.captor.name.ExchangeNameDeriver;
import com.integreety.yatspec.e2e.captor.name.ServiceNameDeriver;
import com.integreety.yatspec.e2e.captor.rabbit.header.HeaderRetriever;
import com.integreety.yatspec.e2e.captor.repository.InterceptedDocumentRepository;
import com.integreety.yatspec.e2e.captor.repository.model.InterceptedCall;
import com.integreety.yatspec.e2e.captor.repository.model.InterceptedCallFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;

import java.util.Collection;
import java.util.Map;

import static com.integreety.yatspec.e2e.captor.repository.model.Type.PUBLISH;
import static com.integreety.yatspec.e2e.captor.template.InteractionMessageTemplates.publishOf;

@RequiredArgsConstructor
public class PublishCaptor {

    private final InterceptedDocumentRepository interceptedDocumentRepository;
    private final InterceptedCallFactory interceptedCallFactory;
    private final ServiceNameDeriver serviceNameDeriver;
    private final ExchangeNameDeriver exchangeNameDeriver;
    private final HeaderRetriever headerRetriever;

    public void capturePublishInteraction(final Message message) {
        final MessageProperties messageProperties = message.getMessageProperties();
        final Map<String, Collection<String>> headers = headerRetriever.retrieve(messageProperties);
        final String interactionName = publishOf(serviceNameDeriver.derive(), exchangeNameDeriver.derive(messageProperties));
        final InterceptedCall interceptedCall = interceptedCallFactory.buildFrom(new String(message.getBody()), headers, interactionName, PUBLISH);
        interceptedDocumentRepository.save(interceptedCall);
    }
}