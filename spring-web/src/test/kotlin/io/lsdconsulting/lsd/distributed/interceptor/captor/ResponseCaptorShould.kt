package io.lsdconsulting.lsd.distributed.interceptor.captor

import io.lsdconsulting.lsd.distributed.interceptor.captor.http.SourceTargetDeriver
import io.lsdconsulting.lsd.distributed.interceptor.persistence.RepositoryService
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

internal class ResponseCaptorShould {
    private val repositoryService = mockk<RepositoryService>(relaxed = true)
    private val sourceTargetDeriver = mockk<SourceTargetDeriver>()
    private val httpRequest = mockk<HttpRequest>()
    private val clientHttpResponse = mockk<ClientHttpResponse>()
    private val httpHeaderRetriever = mockk<HttpHeaderRetriever>()

    private val underTest = ResponseCaptor(repositoryService, sourceTargetDeriver, httpHeaderRetriever, "profile")

    private val traceId = randomAlphanumeric(20)
    private val target = randomAlphanumeric(20)
    private val serviceName = randomAlphanumeric(20)
    private val path = randomAlphanumeric(20)
    private val requestHeaders = mapOf<String, Collection<String>>("b3" to listOf(traceId), "Target-Name" to listOf(target))
    private val responseHeaders = mapOf<String, Collection<String>>()

    @Test
    @Throws(IOException::class)
    fun `handle empty response body from delete request`() {
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
    fun `enqueue intercepted interaction on spring response`() {
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
