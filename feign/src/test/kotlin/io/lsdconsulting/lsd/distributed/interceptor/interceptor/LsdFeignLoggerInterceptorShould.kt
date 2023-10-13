package io.lsdconsulting.lsd.distributed.interceptor.interceptor

import feign.InvocationContext
import feign.Logger
import feign.Request
import feign.RequestTemplate
import io.lsdconsulting.lsd.distributed.connector.model.InterceptedInteraction
import io.lsdconsulting.lsd.distributed.interceptor.captor.FeignRequestCaptor
import io.lsdconsulting.lsd.distributed.interceptor.captor.FeignResponseCaptor
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.apache.commons.lang3.RandomStringUtils
import org.apache.commons.lang3.RandomUtils
import org.jeasy.random.EasyRandom
import org.jeasy.random.EasyRandomParameters
import org.junit.jupiter.api.Test

internal class LsdFeignLoggerInterceptorShould {
    private val easyRandom = EasyRandom(EasyRandomParameters().seed(System.currentTimeMillis()))
    private val feignRequestCaptor = mockk<FeignRequestCaptor>()
    private val feignResponseCaptor = mockk<FeignResponseCaptor>()
    private val requestTemplate = mockk<RequestTemplate>()
    private val invocationContext = mockk<InvocationContext>()
    private val request = mockk<Request>(relaxed = true)
    private val level = Logger.Level.BASIC
    private val elapsedTime = RandomUtils.nextLong()
    private val body = RandomStringUtils.randomAlphanumeric(10)

    private val underTest = LsdFeignLoggerInterceptor(feignRequestCaptor, feignResponseCaptor)

    @Test
    fun `logs request`() {
        every { feignRequestCaptor.captureRequestInteraction(request) } returns easyRandom.nextObject(InterceptedInteraction::class.java)
        every { requestTemplate.request() } returns request
        underTest.apply(requestTemplate)
        verify {  feignRequestCaptor.captureRequestInteraction(request) }
    }

//    @Test
//    fun `log and re-buffer response`() {
//        val request = Request.create(POST, "url", mapOf(), null, UTF_8, null)
//        val response = Response.builder().body(body.byteInputStream(UTF_8), body.length).request(request).build()
//        every { feignResponseCaptor.captureResponseInteraction(response, body, elapsedTime) } returns
//            easyRandom.nextObject(InterceptedInteraction::class.java).copy(body = null)
//        every { invocationContext.response() } returns response
//
//        underTest.aroundDecode(invocationContext)
//
//        verify { feignResponseCaptor.captureResponseInteraction(any<Response>(), body, any<Long>()) }
//    }
}
