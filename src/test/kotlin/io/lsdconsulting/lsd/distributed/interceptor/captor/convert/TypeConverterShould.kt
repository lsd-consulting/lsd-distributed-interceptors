package io.lsdconsulting.lsd.distributed.interceptor.captor.convert

import feign.Response
import io.mockk.every
import io.mockk.mockk
import org.apache.commons.io.IOUtils
import org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.junit.jupiter.api.Test
import java.io.IOException
import java.nio.charset.StandardCharsets

internal class TypeConverterShould {
    @Test
    fun `convert byte array to string`() {
        val body = randomAlphanumeric(20)
        assertThat(body.toByteArray().stringify(), `is`(body))
    }

    @Test
    @Throws(IOException::class)
    fun `convert response body to string`() {
        val body = randomAlphanumeric(20)
        val responseBody = mockk<Response.Body>()
        every { responseBody.asInputStream() } returns IOUtils.toInputStream(body, StandardCharsets.UTF_8)
        assertThat((body.toByteArray()).stringify(), `is`(body))
    }
}
