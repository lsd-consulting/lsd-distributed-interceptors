package io.lsdconsulting.lsd.distributed.interceptor.captor

import feign.Request
import feign.Response
import io.lsdconsulting.lsd.distributed.interceptor.captor.header.Obfuscator
import io.mockk.every
import io.mockk.mockk
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.hasSize
import org.hamcrest.Matchers.`is`
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream

internal class FeignHttpHeaderRetrieverShould {
    private val obfuscator = mockk<Obfuscator>()

    private val underTest = FeignHttpHeaderRetriever(obfuscator)

    @BeforeEach
    fun setup() {
        every { obfuscator.obfuscate(any()) } answers { firstArg() }
    }

    @ParameterizedTest
    @MethodSource("provideHeaders")
    fun `retrieve headers from request`(headers: Map<String, Collection<String>>, expectedSize: Int) {
        val request = mockk<Request>()
        every { request.headers() } returns headers

        val result = underTest.retrieve(request)

        assertThat(result.keys, hasSize(expectedSize))
        headers.keys.forEach {
            assertThat(result[it], `is`(headers[it]))
        }
    }

    @ParameterizedTest
    @MethodSource("provideHeaders")
    fun `retrieve headers from response`(headers: Map<String, Collection<String>>, expectedSize: Int) {
        val response = mockk<Response>()
        every { response.headers() } returns headers

        val result = underTest.retrieve(response)

        assertThat(result.keys, hasSize(expectedSize))
        headers.keys.forEach {
            assertThat(result[it], `is`(headers[it]))
        }
    }

    companion object {
        @JvmStatic
        private fun provideHeaders(): Stream<Arguments> {
            return Stream.of(
                Arguments.of(mapOf("name" to listOf("value")), 1),
                Arguments.of(mapOf("name1" to listOf("value1"), "name2" to listOf("value2")), 2),
                Arguments.of(mutableMapOf<Any, Any>(), 0)
            )
        }
    }
}
