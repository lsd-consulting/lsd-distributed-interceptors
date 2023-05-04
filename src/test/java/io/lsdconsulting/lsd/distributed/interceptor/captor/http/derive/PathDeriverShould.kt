package io.lsdconsulting.lsd.distributed.interceptor.captor.http.derive

import io.mockk.every
import io.mockk.mockk
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.springframework.http.HttpRequest
import java.net.URI

internal class PathDeriverShould {

    private val underTest: PathDeriver = object : PathDeriver() {}

    @ParameterizedTest
    @CsvSource(value = [
        "http://www.bbc.co.uk/somePage.html?abc=def, /somePage.html?abc=def",
        "http://www.bbc.co.uk/somePage.html, /somePage.html",
        "https://www.bbc.co.uk/customer/1/address, /customer/1/address",
        "https://www.bbc.co.uk/, /"
    ])
    fun derivePathFrom(url: String, expectedPath: String) {
        val result = underTest.derivePathFrom(url)
        assertThat(result, `is`(expectedPath))
    }

    @Test
    fun derivePathFromEmptyResource() {
        val result = underTest.derivePathFrom("https://www.bbc.co.uk")
        assertThat(result, `is`(""))
    }

    @ParameterizedTest
    @CsvSource(value = [
        "http://www.bbc.co.uk/somePage.html?abc=def, /somePage.html?abc=def",
        "http://www.bbc.co.uk/somePage.html, /somePage.html",
        "https://www.bbc.co.uk/customer/1/address, /customer/1/address",
        "https://www.bbc.co.uk/, /",
        "https://www.bbc.co.uk/resource/childResource?param=value, /resource/childResource?param=value"
    ])
    fun derivePathFromHttpRequest(url: String, expectedPath: String) {
        val httpRequest = mockk<HttpRequest>()
        every { httpRequest.uri } returns URI.create(url)

        val result = underTest.derivePathFrom(httpRequest)

        assertThat(result, `is`(expectedPath))
    }

    @Test
    fun derivePathFromHttpRequestEmptyResource() {
        val httpRequest = mockk<HttpRequest>()
        every { httpRequest.uri } returns URI.create("https://www.bbc.co.uk")

        val result = underTest.derivePathFrom(httpRequest)

        assertThat(result, `is`(""))
    }
}
