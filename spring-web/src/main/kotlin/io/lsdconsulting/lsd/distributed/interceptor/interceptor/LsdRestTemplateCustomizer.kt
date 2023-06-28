package io.lsdconsulting.lsd.distributed.interceptor.interceptor

import org.springframework.boot.web.client.RestTemplateCustomizer
import org.springframework.http.client.BufferingClientHttpRequestFactory
import org.springframework.http.client.ClientHttpRequestInterceptor
import org.springframework.http.client.SimpleClientHttpRequestFactory
import org.springframework.web.client.RestTemplate

class LsdRestTemplateCustomizer(
    var interceptor: ClientHttpRequestInterceptor
) : RestTemplateCustomizer {

    override fun customize(restTemplate: RestTemplate) {
        val interceptors = restTemplate.interceptors
        if (!interceptors.contains(interceptor)) {
            interceptors.add(interceptor)
        }
        restTemplate.requestFactory = BufferingClientHttpRequestFactory(SimpleClientHttpRequestFactory())
    }
}
