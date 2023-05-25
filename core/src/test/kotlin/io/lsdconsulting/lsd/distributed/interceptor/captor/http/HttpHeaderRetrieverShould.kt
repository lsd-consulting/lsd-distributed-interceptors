package io.lsdconsulting.lsd.distributed.interceptor.captor.http

import io.lsdconsulting.lsd.distributed.interceptor.captor.header.Obfuscator
import io.mockk.every
import io.mockk.mockk
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.hasSize
import org.hamcrest.Matchers.`is`
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpRequest
import org.springframework.http.client.ClientHttpResponse
import org.springframework.util.MultiValueMapAdapter
import java.util.stream.Stream

internal class HttpHeaderRetrieverShould {
    private val obfuscator = mockk<Obfuscator>()

    private val underTest = HttpHeaderRetriever(obfuscator)

    @BeforeEach
    fun setup() {
        every { obfuscator.obfuscate(any()) } answers { firstArg() }
    }

    @ParameterizedTest
    @MethodSource("provideHttpHeaders")
    fun `retrieve headers from http request`(headers: HttpHeaders, expectedSize: Int) {
        val httpRequest = mockk<HttpRequest>()
        every { httpRequest.headers } returns headers

        val result = underTest.retrieve(httpRequest)

        assertThat(result.keys, hasSize(expectedSize))
        headers.keys.forEach {
            assertThat(result[it], `is`(headers[it]))
        }
    }


    @ParameterizedTest
    @MethodSource("provideHttpHeaders")
    fun `retrieve headers from http response`(headers: HttpHeaders, expectedSize: Int) {
        val httpResponse = mockk<ClientHttpResponse>()
        every { httpResponse.headers } returns headers

        val result = underTest.retrieve(httpResponse)

        assertThat(result.keys, hasSize(expectedSize))
        headers.keys.forEach {
            assertThat(result[it], `is`(headers[it]))
        }
    }

    @Test
    fun `handle headers with no values from http request`() {
        val headers = HttpHeaders(MultiValueMapAdapter(mapOf("name" to listOf())))
        val httpRequest = mockk<HttpRequest>()
        every { httpRequest.headers } returns headers

        val result = underTest.retrieve(httpRequest)

        assertThat(result.keys, hasSize(1))
        assertThat(result["name"], `is`(listOf<Any>()))
    }

    @Test
    fun `handle headers with no values from http response`() {
        val headers = HttpHeaders(MultiValueMapAdapter(mapOf("name" to listOf())))
        val httpResponse = mockk<ClientHttpResponse>()
        every { httpResponse.headers } returns headers

        val result = underTest.retrieve(httpResponse)

        assertThat(result.keys, hasSize(1))
        assertThat(result["name"], `is`(listOf<Any>()))
    }

    companion object {
        @JvmStatic
        private fun provideHttpHeaders(): Stream<Arguments> {
            return Stream.of(
                Arguments.of(HttpHeaders(MultiValueMapAdapter(mapOf("name" to listOf("value")))), 1),
                Arguments.of(HttpHeaders(MultiValueMapAdapter(mapOf("name1" to listOf("value1"), "name2" to listOf("value2")))), 2),
                Arguments.of(HttpHeaders(MultiValueMapAdapter(mutableMapOf())), 0)
            )
        }
    }
}
