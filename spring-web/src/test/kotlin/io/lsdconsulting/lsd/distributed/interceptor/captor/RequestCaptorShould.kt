package io.lsdconsulting.lsd.distributed.interceptor.captor

import io.lsdconsulting.lsd.distributed.interceptor.captor.http.SourceTargetDeriver
import io.lsdconsulting.lsd.distributed.interceptor.captor.trace.TraceIdRetriever
import io.lsdconsulting.lsd.distributed.interceptor.persistence.RepositoryService
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.junit.jupiter.api.Test
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod.GET
import org.springframework.http.HttpRequest
import org.springframework.http.HttpStatus
import org.springframework.http.client.ClientHttpResponse
import java.io.IOException
import java.net.URI

internal class RequestCaptorShould {
    private val repositoryService = mockk<RepositoryService>(relaxed = true)
    private val sourceTargetDeriver = mockk<SourceTargetDeriver>()
    private val traceIdRetriever = mockk<TraceIdRetriever>()
    private val httpRequest = mockk<HttpRequest>()
    private val clientHttpResponse = mockk<ClientHttpResponse>()
    private val httpHeaderRetriever = mockk<HttpHeaderRetriever>()

    private val underTest = RequestCaptor(repositoryService, sourceTargetDeriver, traceIdRetriever, httpHeaderRetriever, "profile")

    private val traceId = randomAlphanumeric(20)
    private val target = randomAlphanumeric(20)
    private val serviceName = randomAlphanumeric(20)
    private val requestHeaders = mapOf<String, Collection<String>>("b3" to listOf(traceId), "Target-Name" to listOf(target))

    @Test
    @Throws(IOException::class)
    fun `handle empty response body from delete request`() {
        val httpHeaders = mockk<HttpHeaders>()
        every { httpRequest.headers} returns httpHeaders
        every { httpRequest.method} returns GET
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
    fun `enqueue intercepted interaction on spring response`() {
        val httpHeaders = mockk<HttpHeaders>()
        every { httpRequest.headers} returns httpHeaders
        every { httpRequest.method} returns GET
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
