package com.integreety.yatspec.e2e.interceptor;

import com.integreety.yatspec.e2e.captor.http.RequestCaptor;
import com.integreety.yatspec.e2e.captor.http.ResponseCaptor;
import com.integreety.yatspec.e2e.captor.repository.model.InterceptedInteraction;
import org.junit.jupiter.api.Test;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.web.client.RestTemplate;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

class LsdRestTemplateCustomizerShould {

    private final RequestCaptor requestCaptor = mock(RequestCaptor.class);
    private final ResponseCaptor responseCaptor = mock(ResponseCaptor.class);

    private final RestTemplate restTemplate = new RestTemplate();
    private final LsdRestTemplateInterceptor lsdRestTemplateInterceptor = new LsdRestTemplateInterceptor(requestCaptor, responseCaptor);

    private final LsdRestTemplateCustomizer underTest = new LsdRestTemplateCustomizer(lsdRestTemplateInterceptor);

    @Test
    void addsLsdInterceptor() {
        underTest.customize(restTemplate);

        assertThat(restTemplate.getInterceptors()).containsExactly(lsdRestTemplateInterceptor);
    }

    @Test
    void preservesExistingCustomizers() {
        restTemplate.setInterceptors(List.of(mock(ClientHttpRequestInterceptor.class)));

        underTest.customize(restTemplate);

        assertThat(restTemplate.getInterceptors()).hasSize(2);
    }

    @Test
    void doesntAddDuplicateInterceptor() {
        restTemplate.setInterceptors(List.of(lsdRestTemplateInterceptor));

        underTest.customize(restTemplate);

        assertThat(restTemplate.getInterceptors()).hasSize(1);
    }

    @Test
    void responseIsNotEmptyAfterInterception() {
        given(requestCaptor.captureRequestInteraction(any(), anyString())).willReturn(InterceptedInteraction.builder().build());

        underTest.customize(restTemplate);

        final String forObject = restTemplate.getForObject("https://httpbin.org/get", String.class);

        assertThat(forObject).isNotEmpty();
    }
}
