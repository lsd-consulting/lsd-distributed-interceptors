package com.integreety.yatspec.e2e.config;

import com.integreety.yatspec.e2e.captor.http.RequestCaptor;
import com.integreety.yatspec.e2e.captor.http.ResponseCaptor;
import com.integreety.yatspec.e2e.interceptor.LsdRestTemplateCustomizer;
import com.integreety.yatspec.e2e.interceptor.LsdRestTemplateInterceptor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Configuration
@ConditionalOnProperty(name = "lsd.db.connectionstring")
@ConditionalOnClass(RestTemplate.class)
@RequiredArgsConstructor
public class RestTemplateInterceptorConfig {

    @Bean
    public ClientHttpRequestInterceptor lsdRestTemplateInterceptor(final RequestCaptor requestCaptor, final ResponseCaptor responseCaptor) {
        return new LsdRestTemplateInterceptor(requestCaptor, responseCaptor);
    }

    @Bean
    public LsdRestTemplateCustomizer lsdRestTemplateCustomizer(final ClientHttpRequestInterceptor lsdRestTemplateInterceptor) {
        return new LsdRestTemplateCustomizer(lsdRestTemplateInterceptor);
    }
}