package com.integreety.yatspec.e2e.integration.testapp.config;

import com.integreety.yatspec.e2e.config.*;
import com.integreety.yatspec.e2e.interceptor.LsdRestTemplateCustomizer;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@EnableAutoConfiguration
@Import({
        RabbitConfig.class,
        RabbitTemplateConfiguration.class,
        FeignInterceptorConfig.class,
        LibraryConfig.class,
        PropertyConfig.class,
        RabbitListenerInterceptorConfig.class,
        RabbitTemplateInterceptorConfig.class,
        RestTemplateInterceptorConfig.class,
        TestStateCollectorConfig.class
})
@Configuration
@RequiredArgsConstructor
public class EndToEndConfiguration {

    @Bean
    public TestRestTemplate apicEnabledRestTemplate(final LsdRestTemplateCustomizer customizer) {
        final TestRestTemplate testRestTemplate = new TestRestTemplate();
        customizer.customize(testRestTemplate.getRestTemplate());
        return testRestTemplate;
    }
}
