package io.lsdconsulting.lsd.distributed.interceptor.integration.testapp.config

import io.lsdconsulting.lsd.distributed.interceptor.interceptor.LsdRestTemplateCustomizer
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.context.annotation.Bean

@TestConfiguration
class RestConfig {

    @Bean
    fun testRestTemplate(customizer: LsdRestTemplateCustomizer): TestRestTemplate {
        val testRestTemplate = TestRestTemplate()
        customizer.customize(testRestTemplate.restTemplate)
        return testRestTemplate
    }
}
