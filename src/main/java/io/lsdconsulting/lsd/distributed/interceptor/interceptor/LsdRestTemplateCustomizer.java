package io.lsdconsulting.lsd.distributed.interceptor.interceptor;

import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.springframework.boot.web.client.RestTemplateCustomizer;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Value
@RequiredArgsConstructor
public class LsdRestTemplateCustomizer implements RestTemplateCustomizer {

    ClientHttpRequestInterceptor interceptor;

    @Override
    public void customize(final RestTemplate restTemplate) {
        final List<ClientHttpRequestInterceptor> interceptors = restTemplate.getInterceptors();
        if (!interceptors.contains(interceptor)) {
            interceptors.add(interceptor);
        }
        restTemplate.setRequestFactory(new BufferingClientHttpRequestFactory(new SimpleClientHttpRequestFactory()));
    }
}