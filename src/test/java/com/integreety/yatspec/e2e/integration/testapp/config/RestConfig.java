package com.integreety.yatspec.e2e.integration.testapp.config;

import com.integreety.yatspec.e2e.config.LibraryConfig;
import com.integreety.yatspec.e2e.interceptor.LsdRestTemplateCustomizer;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.core.annotation.Order;

@TestConfiguration
@Order(0)
@AutoConfigureBefore(LibraryConfig.class)
public class RestConfig {

    @Bean
    public TestRestTemplate testRestTemplate(final LsdRestTemplateCustomizer customizer) {
        final TestRestTemplate testRestTemplate = new TestRestTemplate();
        customizer.customize(testRestTemplate.getRestTemplate());
        return testRestTemplate;
    }
}
