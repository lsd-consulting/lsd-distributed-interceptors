package io.lsdconsulting.lsd.distributed.interceptor.captor

import feign.Request
import feign.RequestTemplate
import io.lsdconsulting.lsd.distributed.interceptor.captor.http.SourceTargetDeriver
import io.lsdconsulting.lsd.distributed.interceptor.captor.trace.TraceIdRetriever
import io.lsdconsulting.lsd.distributed.interceptor.persistance.RepositoryService
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.junit.jupiter.api.Test
import java.nio.charset.Charset

internal class FeignRequestCaptorShould {
    private val repositoryService = mockk<RepositoryService>(relaxed = true)
    private val sourceTargetDeriver = mockk<SourceTargetDeriver>()
    private val traceIdRetriever = mockk<TraceIdRetriever>()
    private val feignHttpHeaderRetriever = mockk<FeignHttpHeaderRetriever>()

    private val underTest = FeignRequestCaptor(repositoryService, sourceTargetDeriver, traceIdRetriever, feignHttpHeaderRetriever, "profile")

    private val resource = randomAlphanumeric(20)
    private val url = "http://localhost/$resource"
    private val body = randomAlphanumeric(20)
    private val traceId = randomAlphanumeric(20)
    private val target = randomAlphanumeric(20)
    private val serviceName = randomAlphanumeric(20)
    private val requestHeaders = mapOf<String, Collection<String>>("b3" to listOf(traceId), "Target-Name" to listOf(target))
    private val request = Request.create(Request.HttpMethod.GET, url, requestHeaders, body.toByteArray(), Charset.defaultCharset(), RequestTemplate())

    @Test
    fun `take trace id from request headers`() {
        every { traceIdRetriever.getTraceId(eq(requestHeaders))} returns traceId
        every { feignHttpHeaderRetriever.retrieve(any<Request>()) } returns requestHeaders
        every { sourceTargetDeriver.deriveTarget(eq(requestHeaders), eq("/$resource"))} returns target
        every { sourceTargetDeriver.deriveServiceName(requestHeaders)} returns serviceName

        val (traceId1) = underTest.captureRequestInteraction(request)

        assertThat(traceId1, `is`(traceId))
    }

    @Test
    fun `derive target from request headers`() {
        every { traceIdRetriever.getTraceId(eq(requestHeaders))} returns traceId
        every { sourceTargetDeriver.deriveTarget(eq(requestHeaders), eq("/$resource"))} returns target
        every { sourceTargetDeriver.deriveServiceName(eq(requestHeaders))} returns serviceName
        every { feignHttpHeaderRetriever.retrieve(any<Request>())} returns requestHeaders

        val (_, _, _, _, _, target1) = underTest.captureRequestInteraction(request)

        assertThat(target1, `is`(target))
    }

    @Test
    fun `enqueue intercepted interaction on feign response`() {
        every { traceIdRetriever.getTraceId(eq(requestHeaders))} returns traceId
        every { sourceTargetDeriver.deriveTarget(eq(requestHeaders), eq("/$resource"))} returns target
        every { sourceTargetDeriver.deriveServiceName(eq(requestHeaders))} returns serviceName
        every { feignHttpHeaderRetriever.retrieve(any<Request>())} returns requestHeaders

        val interceptedInteraction = underTest.captureRequestInteraction(request)

        verify { repositoryService.enqueue(interceptedInteraction) }
    }
}
