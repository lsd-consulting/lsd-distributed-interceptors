package io.lsdconsulting.lsd.distributed.interceptor.captor.http

import feign.Request
import feign.Request.HttpMethod.GET
import feign.RequestTemplate
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
import java.net.URI
import java.nio.charset.Charset

internal class RequestCaptorShould {
    private val repositoryService = mockk<RepositoryService>(relaxed = true)
    private val sourceTargetDeriver = mockk<SourceTargetDeriver>()
    private val traceIdRetriever = mockk<TraceIdRetriever>()
    private val httpRequest = mockk<HttpRequest>()
    private val clientHttpResponse = mockk<ClientHttpResponse>()
    private val httpHeaderRetriever = mockk<HttpHeaderRetriever>()

    private val underTest = RequestCaptor(repositoryService, sourceTargetDeriver, traceIdRetriever, httpHeaderRetriever, "profile")

    private val resource = randomAlphanumeric(20)
    private val url = "http://localhost/$resource"
    private val body = randomAlphanumeric(20)
    private val traceId = randomAlphanumeric(20)
    private val target = randomAlphanumeric(20)
    private val serviceName = randomAlphanumeric(20)
    private val requestHeaders = mapOf<String, Collection<String>>("b3" to listOf(traceId), "Target-Name" to listOf(target))
    private val request = Request.create(GET, url, requestHeaders, body.toByteArray(), Charset.defaultCharset(), RequestTemplate())

    @Test
    fun takeTraceIdFromRequestHeaders() {
        every { traceIdRetriever.getTraceId(eq(requestHeaders))} returns traceId
        every { httpHeaderRetriever.retrieve(any<Request>()) } returns requestHeaders
        every { sourceTargetDeriver.deriveTarget(eq(requestHeaders), eq("/$resource"))} returns target
        every { sourceTargetDeriver.deriveServiceName(requestHeaders)} returns serviceName

        val (traceId1) = underTest.captureRequestInteraction(request)

        assertThat(traceId1, `is`(traceId))
    }

    @Test
    fun deriveTargetFromRequestHeaders() {
        every { traceIdRetriever.getTraceId(eq(requestHeaders))} returns traceId
        every { sourceTargetDeriver.deriveTarget(eq(requestHeaders), eq("/$resource"))} returns target
        every { sourceTargetDeriver.deriveServiceName(eq(requestHeaders))} returns serviceName
        every { httpHeaderRetriever.retrieve(any<Request>())} returns requestHeaders

        val (_, _, _, _, _, target1) = underTest.captureRequestInteraction(request)

        assertThat(target1, `is`(target))
    }

    @Test
    fun enqueueInterceptedInteractionOnFeignResponse() {
        every { traceIdRetriever.getTraceId(eq(requestHeaders))} returns traceId
        every { sourceTargetDeriver.deriveTarget(eq(requestHeaders), eq("/$resource"))} returns target
        every { sourceTargetDeriver.deriveServiceName(eq(requestHeaders))} returns serviceName
        every { httpHeaderRetriever.retrieve(any<Request>())} returns requestHeaders

        val interceptedInteraction = underTest.captureRequestInteraction(request)

        verify { repositoryService.enqueue(interceptedInteraction) }
    }

    @Test
    @Throws(IOException::class)
    fun handleEmptyResponseBodyFromDeleteRequest() {
        val httpHeaders = mockk<HttpHeaders>()
        every { httpRequest.headers} returns httpHeaders
        every { httpRequest.methodValue} returns "GET"
        every { httpRequest.uri} returns URI.create("http://localhost/test")
        every { clientHttpResponse.headers} returns httpHeaders
        every { clientHttpResponse.statusCode} returns HttpStatus.NO_CONTENT
        every { httpHeaderRetriever.retrieve(httpRequest)} returns requestHeaders
        every { sourceTargetDeriver.deriveTarget(requestHeaders, "/test")} returns serviceName
        every { sourceTargetDeriver.deriveServiceName(requestHeaders)} returns serviceName
        every { traceIdRetriever.getTraceId(eq(requestHeaders))} returns traceId

        val (_, body1) = underTest.captureRequestInteraction(httpRequest, "body")

        assertThat(body1, `is`("body"))
    }

    @Test
    @Throws(IOException::class)
    fun enqueueInterceptedInteractionOnSpringResponse() {
        val httpHeaders = mockk<HttpHeaders>()
        every { httpRequest.headers} returns httpHeaders
        every { httpRequest.methodValue} returns "GET"
        every { httpRequest.uri} returns URI.create("http://localhost/test")
        every { clientHttpResponse.headers} returns httpHeaders
        every { clientHttpResponse.statusCode} returns HttpStatus.NO_CONTENT
        every { httpHeaderRetriever.retrieve(httpRequest)} returns requestHeaders
        every { sourceTargetDeriver.deriveTarget(requestHeaders, "/test")} returns serviceName
        every { sourceTargetDeriver.deriveServiceName(requestHeaders)} returns serviceName
        every { traceIdRetriever.getTraceId(eq(requestHeaders))} returns traceId

        val interceptedInteraction = underTest.captureRequestInteraction(httpRequest, "body")

        verify { repositoryService.enqueue(interceptedInteraction) }
    }
}
