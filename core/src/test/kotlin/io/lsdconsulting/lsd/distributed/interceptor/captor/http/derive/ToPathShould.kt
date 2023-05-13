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

internal class ToPathShould {

    @ParameterizedTest
    @CsvSource(value = [
        "http://www.bbc.co.uk/somePage.html?abc=def, /somePage.html?abc=def",
        "http://www.bbc.co.uk/somePage.html, /somePage.html",
        "https://www.bbc.co.uk/customer/1/address, /customer/1/address",
        "https://www.bbc.co.uk/, /"
    ])
    fun `derive path from`(url: String, expectedPath: String) {
        val result = url.toPath()
        assertThat(result, `is`(expectedPath))
    }

    @Test
    fun `derive path from empty resource`() {
        val result = "https://www.bbc.co.uk".toPath()
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
    fun `derive path from http request`(url: String, expectedPath: String) {
        val httpRequest = mockk<HttpRequest>()
        every { httpRequest.uri } returns URI.create(url)

        val result = httpRequest.toPath()

        assertThat(result, `is`(expectedPath))
    }

    @Test
    fun `derive path from http request empty resource`() {
        val httpRequest = mockk<HttpRequest>()
        every { httpRequest.uri } returns URI.create("https://www.bbc.co.uk")

        val result = httpRequest.toPath()

        assertThat(result, `is`(""))
    }
}
