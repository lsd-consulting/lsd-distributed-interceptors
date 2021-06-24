package io.lsdconsulting.lsd.distributed.config;

import io.lsdconsulting.lsd.distributed.captor.http.RequestCaptor;
import io.lsdconsulting.lsd.distributed.captor.http.ResponseCaptor;
import io.lsdconsulting.lsd.distributed.interceptor.LsdRestTemplateCustomizer;
import io.lsdconsulting.lsd.distributed.interceptor.LsdRestTemplateInterceptor;
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