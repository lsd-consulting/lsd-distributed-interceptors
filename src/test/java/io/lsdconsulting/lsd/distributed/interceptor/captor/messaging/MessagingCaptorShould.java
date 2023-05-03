package io.lsdconsulting.lsd.distributed.interceptor.captor.messaging;

import io.lsdconsulting.lsd.distributed.access.model.InterceptedInteraction;
import io.lsdconsulting.lsd.distributed.interceptor.captor.common.PropertyServiceNameDeriver;
import io.lsdconsulting.lsd.distributed.interceptor.captor.trace.TraceIdRetriever;
import io.lsdconsulting.lsd.distributed.interceptor.persistance.RepositoryService;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.integration.support.MutableMessage;
import org.springframework.messaging.Message;

import java.util.List;
import java.util.Map;

import static io.lsdconsulting.lsd.distributed.access.model.InteractionType.CONSUME;
import static io.lsdconsulting.lsd.distributed.access.model.InteractionType.PUBLISH;
import static org.apache.commons.lang3.RandomStringUtils.randomAlphabetic;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class MessagingCaptorShould {

    private final RepositoryService repositoryService = mock(RepositoryService.class);
    private final PropertyServiceNameDeriver propertyServiceNameDeriver = mock(PropertyServiceNameDeriver.class);
    private final TraceIdRetriever traceIdRetriever = mock(TraceIdRetriever.class);
    private final MessagingHeaderRetriever messagingHeaderRetriever = mock(MessagingHeaderRetriever.class);

    private final MessagingCaptor underTest = new MessagingCaptor(repositoryService, propertyServiceNameDeriver, traceIdRetriever, messagingHeaderRetriever, "profile");

    private final String topic = randomAlphabetic(20);
    private final String serviceName = randomAlphabetic(20);
    private final String traceId = randomAlphabetic(20);
    private final String body = randomAlphabetic(20);

    @Test
    void captureConsumeInteractionWithSourceFromTypeIdWhenTargetNameNotNoInHeader() {
        given(propertyServiceNameDeriver.getServiceName()).willReturn(serviceName);
        given(traceIdRetriever.getTraceId(any())).willReturn(traceId);
        given(messagingHeaderRetriever.retrieve(any(Message.class))).willReturn(Map.of("name", List.of("value"), "__TypeId__", List.of(topic)));

        Map<String, Object> headers = Map.of("name", List.of("value"), "__TypeId__", topic);
        Message<?> message = new MutableMessage<>(body.getBytes(), headers);

        final InterceptedInteraction result = underTest.captureConsumeInteraction(message);

        assertThat(result.getTarget(), is(topic));
        assertThat(result.getPath(), is(serviceName));
        assertThat(result.getBody(), is(body));
        assertThat(result.getServiceName(), is(serviceName));
        assertThat(result.getTraceId(), is(traceId));
        assertThat(result.getInteractionType(), Matchers.is(CONSUME));
        assertThat(result.getHttpMethod(), emptyOrNullString());
        assertThat(result.getHttpStatus(), emptyOrNullString());
        assertThat(result.getProfile(), is("profile"));
        assertThat(result.getRequestHeaders(), is(Map.of("name", List.of("value"), "__TypeId__", List.of(topic))));
        assertThat(result.getResponseHeaders(), aMapWithSize(0));

        verify(repositoryService).enqueue(result);
    }

    @Test
    void captureConsumeInteractionWithDefaultSourceWhenTargetNotAvailable() {
        given(propertyServiceNameDeriver.getServiceName()).willReturn(serviceName);
        given(traceIdRetriever.getTraceId(any())).willReturn(traceId);
        given(messagingHeaderRetriever.retrieve(any(Message.class))).willReturn(Map.of("name", List.of("value")));

        Map<String, Object> headers = Map.of("name", List.of("value"));
        Message<?> message = new MutableMessage<>(body.getBytes(), headers);

        final InterceptedInteraction result = underTest.captureConsumeInteraction(message);

        assertThat(result.getTarget(), is("UNKNOWN"));
        assertThat(result.getPath(), is(serviceName));
        assertThat(result.getBody(), is(body));
        assertThat(result.getServiceName(), is(serviceName));
        assertThat(result.getTraceId(), is(traceId));
        assertThat(result.getInteractionType(), Matchers.is(CONSUME));
        assertThat(result.getHttpMethod(), emptyOrNullString());
        assertThat(result.getHttpStatus(), emptyOrNullString());
        assertThat(result.getProfile(), is("profile"));
        assertThat(result.getRequestHeaders(), is(Map.of("name", List.of("value"))));
        assertThat(result.getResponseHeaders(), aMapWithSize(0));

        verify(repositoryService).enqueue(result);
    }

    @Test
    void captureConsumeInteractionWithSourceFromTargetName() {
        given(propertyServiceNameDeriver.getServiceName()).willReturn(serviceName);
        given(traceIdRetriever.getTraceId(any())).willReturn(traceId);
        given(messagingHeaderRetriever.retrieve(any(Message.class))).willReturn(Map.of("name", List.of("value"), "Target-Name", List.of(topic), "__TypeId__", List.of("blah")));

        Map<String, Object> headers = Map.of("name", List.of("value"), "__TypeId__", "blah", "Target-Name", topic);
        Message<?> message = new MutableMessage<>(body.getBytes(), headers);

        final InterceptedInteraction result = underTest.captureConsumeInteraction(message);

        assertThat(result.getTarget(), is(topic));
        assertThat(result.getPath(), is(serviceName));
        assertThat(result.getBody(), is(body));
        assertThat(result.getServiceName(), is(serviceName));
        assertThat(result.getTraceId(), is(traceId));
        assertThat(result.getInteractionType(), Matchers.is(CONSUME));
        assertThat(result.getHttpMethod(), emptyOrNullString());
        assertThat(result.getHttpStatus(), emptyOrNullString());
        assertThat(result.getProfile(), is("profile"));
        assertThat(result.getRequestHeaders(), is(Map.of("name", List.of("value"), "__TypeId__", List.of("blah"), "Target-Name", List.of(topic))));
        assertThat(result.getResponseHeaders(), aMapWithSize(0));

        verify(repositoryService).enqueue(result);
    }

    @Test
    void capturePublishInteractionWithSourceFromHeader() {
        given(traceIdRetriever.getTraceId(any())).willReturn(traceId);
        given(messagingHeaderRetriever.retrieve(any(Message.class))).willReturn(Map.of("name", List.of("value")));

        Map<String, Object> headers = Map.of("name", List.of("value"), "Source-Name", serviceName, "Target-Name", topic);
        Message<?> message = new MutableMessage<>(body.getBytes(), headers);

        final InterceptedInteraction result = underTest.capturePublishInteraction(message);

        assertThat(result.getTarget(), is(topic));
        assertThat(result.getPath(), is(topic));
        assertThat(result.getBody(), is(body));
        assertThat(result.getServiceName(), is(serviceName));
        assertThat(result.getTraceId(), is(traceId));
        assertThat(result.getInteractionType(), Matchers.is(PUBLISH));
        assertThat(result.getHttpMethod(), emptyOrNullString());
        assertThat(result.getHttpStatus(), emptyOrNullString());
        assertThat(result.getProfile(), is("profile"));
        assertThat(result.getRequestHeaders(), is(Map.of("name", List.of("value"))));
        assertThat(result.getResponseHeaders(), aMapWithSize(0));

        verify(repositoryService).enqueue(result);
    }

    @Test
    void capturePublishInteractionWithoutSourceFromHeader() {
        given(propertyServiceNameDeriver.getServiceName()).willReturn(serviceName);
        given(traceIdRetriever.getTraceId(any())).willReturn(traceId);
        given(messagingHeaderRetriever.retrieve(any(Message.class))).willReturn(Map.of("name", List.of("value")));

        Map<String, Object> headers = Map.of("name", List.of("value"),"Target-Name", topic);
        Message<?> message = new MutableMessage<>(body.getBytes(), headers);

        final InterceptedInteraction result = underTest.capturePublishInteraction(message);

        assertThat(result.getTarget(), is(topic));
        assertThat(result.getPath(), is(topic));
        assertThat(result.getBody(), is(body));
        assertThat(result.getServiceName(), is(serviceName));
        assertThat(result.getTraceId(), is(traceId));
        assertThat(result.getInteractionType(), Matchers.is(PUBLISH));
        assertThat(result.getHttpMethod(), emptyOrNullString());
        assertThat(result.getHttpStatus(), emptyOrNullString());
        assertThat(result.getProfile(), is("profile"));
        assertThat(result.getRequestHeaders(), is(Map.of("name", List.of("value"))));
        assertThat(result.getResponseHeaders(), aMapWithSize(0));

        verify(repositoryService).enqueue(result);
    }
}
