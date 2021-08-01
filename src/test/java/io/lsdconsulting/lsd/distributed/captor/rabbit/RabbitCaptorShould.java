package io.lsdconsulting.lsd.distributed.captor.rabbit;

import io.lsdconsulting.lsd.distributed.captor.header.HeaderRetriever;
import io.lsdconsulting.lsd.distributed.captor.http.derive.PropertyServiceNameDeriver;
import io.lsdconsulting.lsd.distributed.captor.repository.InterceptedDocumentRepository;
import io.lsdconsulting.lsd.distributed.captor.repository.model.InterceptedInteraction;
import io.lsdconsulting.lsd.distributed.captor.repository.model.InterceptedInteractionFactory;
import io.lsdconsulting.lsd.distributed.captor.repository.model.Type;
import io.lsdconsulting.lsd.distributed.captor.trace.TraceIdRetriever;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static wiremock.org.apache.commons.lang3.RandomStringUtils.randomAlphabetic;

class RabbitCaptorShould {

    private final InterceptedDocumentRepository interceptedDocumentRepository = mock(InterceptedDocumentRepository.class);
    private final PropertyServiceNameDeriver propertyServiceNameDeriver = mock(PropertyServiceNameDeriver.class);
    private final TraceIdRetriever traceIdRetriever = mock(TraceIdRetriever.class);
    private final HeaderRetriever headerRetriever = mock(HeaderRetriever.class);

    private final InterceptedInteractionFactory interceptedInteractionFactory = new InterceptedInteractionFactory("profile");

    private final RabbitCaptor underTest = new RabbitCaptor(interceptedDocumentRepository, interceptedInteractionFactory, propertyServiceNameDeriver, traceIdRetriever, headerRetriever);

    private final String exchange = randomAlphabetic(20);
    private final String serviceName = randomAlphabetic(20);
    private final String traceId = randomAlphabetic(20);
    private final String body = randomAlphabetic(20);
    private final MessageProperties messageProperties = new MessageProperties();
    private final Message message = new Message(body.getBytes(), messageProperties);
    private final Map<String, Collection<String>> headers = Map.of("name", List.of("value"));

    @Test
    void captureAmqpInteraction() {
        given(propertyServiceNameDeriver.getServiceName()).willReturn(serviceName);
        given(traceIdRetriever.getTraceId(any())).willReturn(traceId);
        given(headerRetriever.retrieve(any(Message.class))).willReturn(headers);
        messageProperties.setHeader("name", "value");

        final InterceptedInteraction result = underTest.captureInteraction(exchange, message, Type.PUBLISH);

        assertThat(result.getPath(), is(exchange));
        assertThat(result.getBody(), is(body));
        assertThat(result.getServiceName(), is(serviceName));
        assertThat(result.getTraceId(), is(traceId));
        assertThat(result.getType(), Matchers.is(Type.PUBLISH));
        assertThat(result.getHttpMethod(), emptyOrNullString());
        assertThat(result.getHttpStatus(), emptyOrNullString());
        assertThat(result.getProfile(), is("profile"));
        assertThat(result.getRequestHeaders(), is(headers));
        assertThat(result.getResponseHeaders(), aMapWithSize(0));
    }
}