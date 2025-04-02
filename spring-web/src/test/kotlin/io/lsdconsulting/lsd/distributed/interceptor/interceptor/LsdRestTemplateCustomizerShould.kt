package io.lsdconsulting.lsd.distributed.interceptor.interceptor

import io.github.krandom.KRandom
import io.github.krandom.KRandomParameters
import io.lsdconsulting.lsd.distributed.connector.model.InterceptedInteraction
import io.lsdconsulting.lsd.distributed.interceptor.captor.RequestCaptor
import io.lsdconsulting.lsd.distributed.interceptor.captor.ResponseCaptor
import io.mockk.every
import io.mockk.mockk
import org.hamcrest.CoreMatchers.nullValue
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.*
import org.junit.jupiter.api.Test
import org.springframework.http.client.ClientHttpRequestInterceptor
import org.springframework.web.client.RestTemplate

internal class LsdRestTemplateCustomizerShould {
    private val kRandom = KRandom(KRandomParameters().seed(System.currentTimeMillis()))
    private val requestCaptor = mockk<RequestCaptor>()
    private val responseCaptor = mockk<ResponseCaptor>()
    private val restTemplate = RestTemplate()
    private val lsdRestTemplateInterceptor = LsdRestTemplateInterceptor(requestCaptor, responseCaptor)

    private val underTest = LsdRestTemplateCustomizer(lsdRestTemplateInterceptor)

    @Test
    fun addsLsdInterceptor() {
        underTest.customize(restTemplate)

        assertThat(restTemplate.interceptors, contains(lsdRestTemplateInterceptor))
    }

    @Test
    fun `preserves existing customizers`() {
        restTemplate.interceptors = listOf(mockk())

        underTest.customize(restTemplate)

        assertThat(restTemplate.interceptors, hasSize(2))
    }

    @Test
    fun `doesnt add duplicate interceptor`() {
        restTemplate.interceptors = listOf<ClientHttpRequestInterceptor>(lsdRestTemplateInterceptor)

        underTest.customize(restTemplate)

        assertThat(restTemplate.interceptors, hasSize(1))
    }

    @Test
    fun `response is not empty after interception`() {
        every { requestCaptor.captureRequestInteraction(any(), any()) } returns kRandom.nextObject(InterceptedInteraction::class.java)

        underTest.customize(restTemplate)

        val forObject = restTemplate.getForObject("https://bbc.co.uk", String::class.java)
        assertThat(forObject, not(nullValue()))
    }
}
