package io.lsdconsulting.lsd.distributed.interceptor.captor.http

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

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
}
