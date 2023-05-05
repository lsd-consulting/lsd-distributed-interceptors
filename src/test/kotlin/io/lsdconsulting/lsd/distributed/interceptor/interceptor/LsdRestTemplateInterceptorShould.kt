package io.lsdconsulting.lsd.distributed.interceptor.interceptor

import io.lsdconsulting.lsd.distributed.access.model.InterceptedInteraction
import io.lsdconsulting.lsd.distributed.interceptor.captor.http.RequestCaptor
import io.lsdconsulting.lsd.distributed.interceptor.captor.http.ResponseCaptor
import io.mockk.*
import org.apache.commons.lang3.RandomStringUtils.randomAlphabetic
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.jeasy.random.EasyRandom
import org.jeasy.random.EasyRandomParameters
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.HttpRequest
import org.springframework.http.client.ClientHttpRequestExecution
import org.springframework.http.client.ClientHttpResponse

internal class LsdRestTemplateInterceptorShould {
    private val easyRandom = EasyRandom(EasyRandomParameters().seed(System.currentTimeMillis()))

    private val requestCaptor = mockk<RequestCaptor>()
    private val responseCaptor = mockk<ResponseCaptor>(relaxed = true)
    private val httpRequest = mockk<HttpRequest>()
    private val httpResponse = mockk<ClientHttpResponse>()
    private val execution = mockk<ClientHttpRequestExecution>()
    private val underTest = LsdRestTemplateInterceptor(requestCaptor, responseCaptor)
    private val body = randomAlphabetic(20)
    private val target = randomAlphabetic(20)
    private val path = randomAlphabetic(20)
    private val traceId = randomAlphabetic(20)

    @BeforeEach
    fun setup() {
        every { requestCaptor.captureRequestInteraction(any(), eq(body)) } returns
                easyRandom.nextObject(InterceptedInteraction::class.java)
                    .copy(target = target, path = path, traceId = traceId)
        every { execution.execute(any(), any()) } returns httpResponse
    }

    @Test
    fun `pass actual request to execution`() {
        every { requestCaptor.captureRequestInteraction(any(), eq(body)) } returns
                easyRandom.nextObject(InterceptedInteraction::class.java)

        underTest.intercept(httpRequest, body.toByteArray(), execution)

        verify { execution.execute(httpRequest, body.toByteArray()) }
    }

    @Test
    fun `calculate elapsed time`() {
        mockkObject(TimeHelper)
        every { TimeHelper.getNow() } returns 10000000L andThen 20000000L

        every { requestCaptor.captureRequestInteraction(any(), eq(body)) } returns
                easyRandom.nextObject(InterceptedInteraction::class.java)
        val slot = slot<Long>()
        every { responseCaptor.captureResponseInteraction(any(), any(), any(), any(), any(), capture(slot))} returns
                easyRandom.nextObject(InterceptedInteraction::class.java)

        underTest.intercept(httpRequest, body.toByteArray(), execution)

        assertThat(slot.captured, `is`(10L))
        clearAllMocks()
    }

    @Test
    fun `return actual response`() {
        every { requestCaptor.captureRequestInteraction(any(), eq(body)) } returns
                easyRandom.nextObject(InterceptedInteraction::class.java)

        val interceptedResponse = underTest.intercept(httpRequest, body.toByteArray(), execution)

        assertThat(interceptedResponse, `is`(httpResponse))
    }

    @Test
    fun `log request interaction`() {
        every { requestCaptor.captureRequestInteraction(any(), eq(body)) } returns
                easyRandom.nextObject(InterceptedInteraction::class.java)

        underTest.intercept(httpRequest, body.toByteArray(), execution)

        verify { requestCaptor.captureRequestInteraction(eq(httpRequest), eq(body)) }
    }

    @Test
    fun `log response interaction`() {
        underTest.intercept(httpRequest, body.toByteArray(), execution)
        verify {
            responseCaptor.captureResponseInteraction(
                eq(httpRequest),
                eq(httpResponse),
                eq(target),
                eq(path),
                eq(traceId),
                any()
            )
        }
    }

    @Test
    fun `abort capturing response when capturing request failed`() {
        every { requestCaptor.captureRequestInteraction(any(), any()) } throws RuntimeException("Error")

        val interceptedResponse = underTest.intercept(httpRequest, body.toByteArray(), execution)

        verify { responseCaptor wasNot Called }
        verify { execution.execute(httpRequest, body.toByteArray()) }
        assertThat(interceptedResponse, `is`(httpResponse))
    }
}
