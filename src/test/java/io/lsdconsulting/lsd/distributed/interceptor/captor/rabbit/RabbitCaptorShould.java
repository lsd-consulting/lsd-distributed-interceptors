package io.lsdconsulting.lsd.distributed.interceptor.captor.rabbit;

import io.lsdconsulting.lsd.distributed.access.model.InterceptedInteraction;
import io.lsdconsulting.lsd.distributed.interceptor.captor.common.PropertyServiceNameDeriver;
import io.lsdconsulting.lsd.distributed.interceptor.captor.trace.TraceIdRetriever;
import io.lsdconsulting.lsd.distributed.interceptor.persistance.RepositoryService;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import static io.lsdconsulting.lsd.distributed.access.model.InteractionType.PUBLISH;
import static org.apache.commons.lang3.RandomStringUtils.randomAlphabetic;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

class RabbitCaptorShould {

    private final RepositoryService repositoryService = mock(RepositoryService.class);
    private final PropertyServiceNameDeriver propertyServiceNameDeriver = mock(PropertyServiceNameDeriver.class);
    private final TraceIdRetriever traceIdRetriever = mock(TraceIdRetriever.class);
    private final AmqpHeaderRetriever amqpHeaderRetriever = mock(AmqpHeaderRetriever.class);

    private final RabbitCaptor underTest = new RabbitCaptor(repositoryService, propertyServiceNameDeriver, traceIdRetriever, amqpHeaderRetriever, "profile");

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
        given(amqpHeaderRetriever.retrieve(any(Message.class))).willReturn(headers);
        messageProperties.setHeader("name", "value");

        final InterceptedInteraction result = underTest.captureInteraction(exchange, message, PUBLISH);

        assertThat(result.getPath(), is(exchange));
        assertThat(result.getBody(), is(body));
        assertThat(result.getServiceName(), is(serviceName));
        assertThat(result.getTraceId(), is(traceId));
        assertThat(result.getInteractionType(), Matchers.is(PUBLISH));
        assertThat(result.getHttpMethod(), emptyOrNullString());
        assertThat(result.getHttpStatus(), emptyOrNullString());
        assertThat(result.getProfile(), is("profile"));
        assertThat(result.getRequestHeaders(), is(headers));
        assertThat(result.getResponseHeaders(), aMapWithSize(0));
    }
}