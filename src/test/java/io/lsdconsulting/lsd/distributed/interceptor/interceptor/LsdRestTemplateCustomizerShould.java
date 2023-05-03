package io.lsdconsulting.lsd.distributed.interceptor.interceptor;

import io.lsdconsulting.lsd.distributed.access.model.InterceptedInteraction;
import io.lsdconsulting.lsd.distributed.interceptor.captor.http.RequestCaptor;
import io.lsdconsulting.lsd.distributed.interceptor.captor.http.ResponseCaptor;
import org.jeasy.random.EasyRandom;
import org.jeasy.random.EasyRandomParameters;
import org.junit.jupiter.api.Test;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.web.client.RestTemplate;

import java.util.List;

import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

class LsdRestTemplateCustomizerShould {
    private final EasyRandom easyRandom = new EasyRandom(new EasyRandomParameters().seed(System.currentTimeMillis()));

    private final RequestCaptor requestCaptor = mock(RequestCaptor.class);
    private final ResponseCaptor responseCaptor = mock(ResponseCaptor.class);

    private final RestTemplate restTemplate = new RestTemplate();
    private final LsdRestTemplateInterceptor lsdRestTemplateInterceptor = new LsdRestTemplateInterceptor(requestCaptor, responseCaptor);

    private final LsdRestTemplateCustomizer underTest = new LsdRestTemplateCustomizer(lsdRestTemplateInterceptor);

    @Test
    void addsLsdInterceptor() {
        underTest.customize(restTemplate);

        assertThat(restTemplate.getInterceptors(), contains(lsdRestTemplateInterceptor));
    }

    @Test
    void preservesExistingCustomizers() {
        restTemplate.setInterceptors(List.of(mock(ClientHttpRequestInterceptor.class)));

        underTest.customize(restTemplate);

        assertThat(restTemplate.getInterceptors(), hasSize(2));
    }

    @Test
    void doesntAddDuplicateInterceptor() {
        restTemplate.setInterceptors(List.of(lsdRestTemplateInterceptor));

        underTest.customize(restTemplate);

        assertThat(restTemplate.getInterceptors(), hasSize(1));
    }

    @Test
    void responseIsNotEmptyAfterInterception() {
        given(requestCaptor.captureRequestInteraction(any(), anyString())).willReturn(easyRandom.nextObject(InterceptedInteraction.class));

        underTest.customize(restTemplate);

        final String forObject = restTemplate.getForObject("https://httpbin.org/get", String.class);

        assertThat(forObject, not(nullValue()));
    }
}
