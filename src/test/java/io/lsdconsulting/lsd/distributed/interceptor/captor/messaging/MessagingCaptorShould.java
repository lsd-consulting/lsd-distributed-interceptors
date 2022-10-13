package io.lsdconsulting.lsd.distributed.interceptor.captor.messaging;

import io.lsdconsulting.lsd.distributed.access.model.InterceptedInteraction;
import io.lsdconsulting.lsd.distributed.access.repository.InterceptedDocumentRepository;
import io.lsdconsulting.lsd.distributed.interceptor.captor.header.HeaderRetriever;
import io.lsdconsulting.lsd.distributed.interceptor.captor.http.derive.PropertyServiceNameDeriver;
import io.lsdconsulting.lsd.distributed.interceptor.captor.trace.TraceIdRetriever;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.integration.support.MutableMessage;
import org.springframework.messaging.Message;

import java.util.List;
import java.util.Map;

import static io.lsdconsulting.lsd.distributed.access.model.Type.CONSUME;
import static io.lsdconsulting.lsd.distributed.access.model.Type.PUBLISH;
import static org.apache.commons.lang3.RandomStringUtils.randomAlphabetic;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class MessagingCaptorShould {

    private final InterceptedDocumentRepository interceptedDocumentRepository = mock(InterceptedDocumentRepository.class);
    private final PropertyServiceNameDeriver propertyServiceNameDeriver = mock(PropertyServiceNameDeriver.class);
    private final TraceIdRetriever traceIdRetriever = mock(TraceIdRetriever.class);
    private final HeaderRetriever headerRetriever = mock(HeaderRetriever.class);

    private final MessagingCaptor underTest = new MessagingCaptor(interceptedDocumentRepository, propertyServiceNameDeriver, traceIdRetriever, headerRetriever, "profile");

    private final String topic = randomAlphabetic(20);
    private final String serviceName = randomAlphabetic(20);
    private final String traceId = randomAlphabetic(20);
    private final String body = randomAlphabetic(20);

    @Test
    void captureConsumeInteraction() {
        given(propertyServiceNameDeriver.getServiceName()).willReturn(serviceName);
        given(traceIdRetriever.getTraceId(any())).willReturn(traceId);
        given(headerRetriever.retrieve(any(Message.class))).willReturn(Map.of("name", List.of("value"), "__TypeId__", List.of(topic)));

        Map<String, Object> headers = Map.of("name", List.of("value"), "__TypeId__", topic);
        Message<?> message = new MutableMessage<>(body.getBytes(), headers);

        final InterceptedInteraction result = underTest.captureConsumeInteraction(message);

        assertThat(result.getTarget(), is(topic));
        assertThat(result.getPath(), is(serviceName));
        assertThat(result.getBody(), is(body));
        assertThat(result.getServiceName(), is(serviceName));
        assertThat(result.getTraceId(), is(traceId));
        assertThat(result.getType(), Matchers.is(CONSUME));
        assertThat(result.getHttpMethod(), emptyOrNullString());
        assertThat(result.getHttpStatus(), emptyOrNullString());
        assertThat(result.getProfile(), is("profile"));
        assertThat(result.getRequestHeaders(), is(Map.of("name", List.of("value"), "__TypeId__", List.of(topic))));
        assertThat(result.getResponseHeaders(), aMapWithSize(0));

        verify(interceptedDocumentRepository).save(result);
    }

    @Test
    void capturePublishInteractionWithSourceFromHeader() {
        given(traceIdRetriever.getTraceId(any())).willReturn(traceId);
        given(headerRetriever.retrieve(any(Message.class))).willReturn(Map.of("name", List.of("value")));

        Map<String, Object> headers = Map.of("name", List.of("value"), "Source-Name", serviceName, "Target-Name", topic);
        Message<?> message = new MutableMessage<>(body.getBytes(), headers);

        final InterceptedInteraction result = underTest.capturePublishInteraction(message);

        assertThat(result.getTarget(), is(topic));
        assertThat(result.getPath(), is(topic));
        assertThat(result.getBody(), is(body));
        assertThat(result.getServiceName(), is(serviceName));
        assertThat(result.getTraceId(), is(traceId));
        assertThat(result.getType(), Matchers.is(PUBLISH));
        assertThat(result.getHttpMethod(), emptyOrNullString());
        assertThat(result.getHttpStatus(), emptyOrNullString());
        assertThat(result.getProfile(), is("profile"));
        assertThat(result.getRequestHeaders(), is(Map.of("name", List.of("value"))));
        assertThat(result.getResponseHeaders(), aMapWithSize(0));

        verify(interceptedDocumentRepository).save(result);
    }

    @Test
    void capturePublishInteractionWithoutSourceFromHeader() {
        given(propertyServiceNameDeriver.getServiceName()).willReturn(serviceName);
        given(traceIdRetriever.getTraceId(any())).willReturn(traceId);
        given(headerRetriever.retrieve(any(Message.class))).willReturn(Map.of("name", List.of("value")));

        Map<String, Object> headers = Map.of("name", List.of("value"),"Target-Name", topic);
        Message<?> message = new MutableMessage<>(body.getBytes(), headers);

        final InterceptedInteraction result = underTest.capturePublishInteraction(message);

        assertThat(result.getTarget(), is(topic));
        assertThat(result.getPath(), is(topic));
        assertThat(result.getBody(), is(body));
        assertThat(result.getServiceName(), is(serviceName));
        assertThat(result.getTraceId(), is(traceId));
        assertThat(result.getType(), Matchers.is(PUBLISH));
        assertThat(result.getHttpMethod(), emptyOrNullString());
        assertThat(result.getHttpStatus(), emptyOrNullString());
        assertThat(result.getProfile(), is("profile"));
        assertThat(result.getRequestHeaders(), is(Map.of("name", List.of("value"))));
        assertThat(result.getResponseHeaders(), aMapWithSize(0));

        verify(interceptedDocumentRepository).save(result);
    }
}