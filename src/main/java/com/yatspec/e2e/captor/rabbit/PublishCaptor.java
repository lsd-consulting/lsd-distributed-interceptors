package com.yatspec.e2e.captor.rabbit;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yatspec.e2e.captor.http.ObjectMapperCreator;
import com.yatspec.e2e.captor.name.ExchangeNameDeriver;
import com.yatspec.e2e.captor.name.ServiceNameDeriver;
import com.yatspec.e2e.captor.rabbit.header.HeaderRetriever;
import com.yatspec.e2e.captor.repository.InterceptedDocumentRepository;
import com.yatspec.e2e.captor.repository.MapGenerator;
import lombok.RequiredArgsConstructor;
import org.bson.Document;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;

import java.util.Collection;
import java.util.Map;

import static com.fasterxml.jackson.databind.SerializationFeature.INDENT_OUTPUT;
import static com.yatspec.e2e.captor.repository.Type.PUBLISH;
import static com.yatspec.e2e.captor.template.InteractionMessageTemplates.publishOf;

@RequiredArgsConstructor
public class PublishCaptor {

    private final ObjectMapper objectMapper = new ObjectMapperCreator().getObjectMapper().enable(INDENT_OUTPUT);

    private final InterceptedDocumentRepository interceptedDocumentRepository;
    private final MapGenerator mapGenerator;
    private final ServiceNameDeriver serviceNameDeriver;
    private final ExchangeNameDeriver exchangeNameDeriver;
    private final HeaderRetriever headerRetriever;

    public void capturePublishInteraction(final Message message) throws JsonProcessingException {
        final MessageProperties messageProperties = message.getMessageProperties();
        final Map<String, Collection<String>> headers = headerRetriever.retrieve(messageProperties);
        final String interactionName = publishOf(serviceNameDeriver.derive(), exchangeNameDeriver.derive(messageProperties));
        final Map<String, Object> map = mapGenerator.generateFrom(new String(message.getBody()), headers, interactionName, PUBLISH);
        final Document document = Document.parse(objectMapper.writeValueAsString(map));
        interceptedDocumentRepository.save(document);
    }
}