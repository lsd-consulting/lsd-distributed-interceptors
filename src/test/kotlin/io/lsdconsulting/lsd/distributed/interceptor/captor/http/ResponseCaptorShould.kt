package io.lsdconsulting.lsd.distributed.interceptor.captor.http

import feign.Request
import feign.Request.HttpMethod.GET
import feign.RequestTemplate
import feign.Response
import io.lsdconsulting.lsd.distributed.interceptor.captor.http.derive.PathDeriver
import io.lsdconsulting.lsd.distributed.interceptor.captor.http.derive.SourceTargetDeriver
import io.lsdconsulting.lsd.distributed.interceptor.captor.trace.TraceIdRetriever
import io.lsdconsulting.lsd.distributed.interceptor.persistance.RepositoryService
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.junit.jupiter.api.Test
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpRequest
import org.springframework.http.HttpStatus
import org.springframework.http.client.ClientHttpResponse
import java.io.IOException
import java.nio.charset.Charset

internal class ResponseCaptorShould {
    private val repositoryService = mockk<RepositoryService>(relaxed = true)
    private val sourceTargetDeriver = mockk<SourceTargetDeriver>()
    private val traceIdRetriever = mockk<TraceIdRetriever>()
    private val httpRequest = mockk<HttpRequest>()
    private val clientHttpResponse = mockk<ClientHttpResponse>()
    private val httpHeaderRetriever = mockk<HttpHeaderRetriever>()
    private val pathDeriver = PathDeriver()

    private val underTest = ResponseCaptor(repositoryService, sourceTargetDeriver, pathDeriver, traceIdRetriever, httpHeaderRetriever, "profile")

    private val resource = randomAlphanumeric(20)
    private val url = "http://localhost/$resource"
    private val body = randomAlphanumeric(20)
    private val traceId = randomAlphanumeric(20)
    private val target = randomAlphanumeric(20)
    private val serviceName = randomAlphanumeric(20)
    private val path = randomAlphanumeric(20)
    private val requestHeaders = mapOf<String, Collection<String>>("b3" to listOf(traceId), "Target-Name" to listOf(target))
    private val responseHeaders = mapOf<String, Collection<String>>()
    private val response = Response.builder().request(Request.create(GET, url, requestHeaders, body.toByteArray(), Charset.defaultCharset(), RequestTemplate())).build()

    @Test
    fun takeTraceIdFromRequestHeaders() {
        every { traceIdRetriever.getTraceId(eq(requestHeaders)) } returns traceId
        every { sourceTargetDeriver.deriveTarget(eq(requestHeaders), eq("/$resource")) } returns target
        every { sourceTargetDeriver.deriveServiceName(requestHeaders) } returns serviceName
        every { httpHeaderRetriever.retrieve(any<Request>()) } returns requestHeaders
        every { httpHeaderRetriever.retrieve(any<Response>()) } returns responseHeaders

        val (traceId) = underTest.captureResponseInteraction(response, 10L)

        assertThat(traceId, `is`(this.traceId))
    }

    @Test
    fun deriveTargetFromRequestHeaders() {
        every { traceIdRetriever.getTraceId(eq(requestHeaders)) } returns traceId
        every { sourceTargetDeriver.deriveTarget(eq(requestHeaders), eq("/$resource")) } returns target
        every { sourceTargetDeriver.deriveServiceName(eq(requestHeaders)) } returns serviceName
        every { httpHeaderRetriever.retrieve(any<Request>()) } returns requestHeaders
        every { httpHeaderRetriever.retrieve(any<Response>()) } returns responseHeaders

        val (_, _, _, _, _, target1) = underTest.captureResponseInteraction(response, 10L)

        assertThat(target1, `is`(target))
    }

    @Test
    fun enqueueInterceptedInteractionOnFeignResponse() {
        every { traceIdRetriever.getTraceId(eq(requestHeaders)) } returns traceId
        every { sourceTargetDeriver.deriveTarget(eq(requestHeaders), eq("/$resource")) } returns target
        every { sourceTargetDeriver.deriveServiceName(eq(requestHeaders)) } returns serviceName
        every { httpHeaderRetriever.retrieve(any<Request>()) } returns requestHeaders
        every { httpHeaderRetriever.retrieve(any<Response>()) } returns responseHeaders

        val interceptedInteraction = underTest.captureResponseInteraction(response, 10L)

        verify { repositoryService.enqueue(interceptedInteraction) }
    }

    @Test
    @Throws(IOException::class)
    fun handleEmptyResponseBodyFromDeleteRequest() {
        val httpHeaders = mockk<HttpHeaders>()
        every { httpHeaders.contentLength } returns 10
        every { httpRequest.headers } returns httpHeaders
        every { clientHttpResponse.headers } returns httpHeaders
        every { clientHttpResponse.statusCode } returns HttpStatus.NO_CONTENT
        every { clientHttpResponse.body } returns "body".byteInputStream()
        every { sourceTargetDeriver.deriveServiceName(requestHeaders) } returns serviceName
        every { httpHeaderRetriever.retrieve(httpRequest) } returns requestHeaders
        every { httpHeaderRetriever.retrieve(any<ClientHttpResponse>()) } returns responseHeaders

        val (_, body) = underTest.captureResponseInteraction(httpRequest, clientHttpResponse, target, path, traceId, 10L)

        assertThat(body, `is`("body"))
    }

    @Test
    @Throws(IOException::class)
    fun enqueueInterceptedInteractionOnSpringResponse() {
        val httpHeaders = mockk<HttpHeaders>()
        every { httpHeaders.contentLength } returns 10
        every { httpRequest.headers } returns httpHeaders
        every { clientHttpResponse.headers } returns httpHeaders
        every { clientHttpResponse.statusCode } returns HttpStatus.NO_CONTENT
        every { clientHttpResponse.body } returns "".byteInputStream()
        every { httpHeaderRetriever.retrieve(httpRequest) } returns requestHeaders
        every { sourceTargetDeriver.deriveServiceName(requestHeaders) } returns serviceName
        every { httpHeaderRetriever.retrieve(any<ClientHttpResponse>()) } returns responseHeaders

        val interceptedInteraction = underTest.captureResponseInteraction(httpRequest, clientHttpResponse, target, path, traceId, 10L)

        verify { repositoryService.enqueue(interceptedInteraction) }
    }
}
