package io.lsdconsulting.lsd.distributed.interceptor.captor.trace

import brave.Span
import brave.Tracer
import brave.propagation.TraceContext
import io.mockk.every
import io.mockk.mockk
import org.apache.commons.lang3.RandomStringUtils.randomAlphabetic
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

internal class TraceIdRetrieverShould {
    private val tracer = mockk<Tracer>()
    private val span = mockk<Span>()
    private val context = mockk<TraceContext>()

    private val underTest = TraceIdRetriever(tracer)

    private val traceId = randomAlphabetic(10)

    @Test
    fun retrieveTraceIdFromB3Header() {
        val xRequestInfoValue = listOf("$traceId-$traceId-1")
        val headers = mapOf<String, Collection<String>>("b3" to xRequestInfoValue)

        val result = underTest.getTraceId(headers)

        assertThat(result, `is`(traceId))
    }

    @Test
    fun retrieveTraceIdFromXRequestInfoHeader() {
        val xRequestInfoValue = listOf("referenceId=$traceId;")
        val headers = mapOf<String, Collection<String>>("X-Request-Info" to xRequestInfoValue)

        val result = underTest.getTraceId(headers)

        assertThat(result, `is`(traceId))
    }

    @ParameterizedTest
    @CsvSource(value = ["referenceId=123456", "something=654321;referenceId=123456"])
    fun allowFlexibleFormattingOfXRequestInfoHeaderValue(headerValue: String) {
        val headers = mapOf<String, Collection<String>>("X-Request-Info" to listOf(headerValue))

        val result = underTest.getTraceId(headers)

        assertThat(result, `is`("123456"))
    }

    @ParameterizedTest
    @CsvSource(value = ["reference=123456;", "reference:123456;"])
    fun fallBackToTracerForWrongXRequestInfoHeaderValue(headerValue: String) {
        every { tracer.currentSpan() } returns span
        every { span.context() } returns context
        every { context.traceIdString() } returns "123456"
        val headers = mapOf<String, Collection<String>>("X-Request-Info" to listOf(headerValue))

        val result = underTest.getTraceId(headers)

        assertThat(result, `is`("123456"))
    }

    @Test
    fun retrieveTraceIdFromTracerCurrentSpan() {
        every { tracer.currentSpan() } returns span
        every { span.context() } returns context
        every { context.traceIdString() } returns traceId

        val result = underTest.getTraceId(HashMap())

        assertThat(result, `is`(traceId))
    }

    @Test
    fun retrieveTraceIdFromTracerNextSpan() {
        every { tracer.nextSpan() } returns span
        every { tracer.currentSpan() } returns span
        every { span.context() } returns context
        every { context.traceIdString() } returns traceId

        val result = underTest.getTraceId(HashMap())

        assertThat(result, `is`(traceId))
    }
}
