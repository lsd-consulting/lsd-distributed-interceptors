package io.lsdconsulting.lsd.distributed.captor.http;

import feign.Request;
import feign.RequestTemplate;
import feign.Response;
import io.lsdconsulting.lsd.distributed.captor.http.derive.PathDeriver;
import io.lsdconsulting.lsd.distributed.captor.http.derive.SourceTargetDeriver;
import io.lsdconsulting.lsd.distributed.captor.repository.InterceptedDocumentRepository;
import io.lsdconsulting.lsd.distributed.captor.repository.model.InterceptedInteraction;
import io.lsdconsulting.lsd.distributed.captor.repository.model.InterceptedInteractionFactory;
import io.lsdconsulting.lsd.distributed.captor.trace.TraceIdRetriever;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import static feign.Request.HttpMethod.GET;
import static java.nio.charset.Charset.defaultCharset;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static wiremock.org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;

class ResponseCaptorShould {

    private final InterceptedDocumentRepository interceptedDocumentRepository = mock(InterceptedDocumentRepository.class);
    private final SourceTargetDeriver sourceTargetDeriver = mock(SourceTargetDeriver.class);
    private final TraceIdRetriever traceIdRetriever = mock(TraceIdRetriever.class);

    private final PathDeriver pathDeriver = new PathDeriver();
    private final InterceptedInteractionFactory interceptedInteractionFactory = new InterceptedInteractionFactory("profile");

    private final ResponseCaptor underTest = new ResponseCaptor(interceptedDocumentRepository, interceptedInteractionFactory, sourceTargetDeriver, pathDeriver, traceIdRetriever);

    private final String url = randomAlphanumeric(20);
    private final String body = randomAlphanumeric(20);
    private final String traceId = randomAlphanumeric(20);
    private final String target = randomAlphanumeric(20);
    private final Map<String, Collection<String>> requestHeaders = Map.of("b3", List.of(traceId), "Target-Name", List.of(target));

    private final Response response = Response.builder()
            .request(Request.create(GET, url, requestHeaders, body.getBytes(), defaultCharset(), new RequestTemplate( )))
            .build();

    @Test
    public void takeTraceIdFromRequestHeaders() {
        given(traceIdRetriever.getTraceId(eq(requestHeaders))).willReturn(traceId);

        final InterceptedInteraction interceptedInteraction = underTest.captureResponseInteraction(response);

        assertThat(interceptedInteraction.getTraceId(), is(traceId));
    }

    @Test
    public void deriveTargetFromRequestHeaders() {
       given(sourceTargetDeriver.deriveTarget(eq(requestHeaders), eq(url))).willReturn(target);

        final InterceptedInteraction interceptedInteraction = underTest.captureResponseInteraction(response);

        assertThat(interceptedInteraction.getTarget(), is(target));
    }
}