package io.lsdconsulting.lsd.distributed.interceptor.captor.common

import org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.junit.jupiter.api.Test

internal class TypeConverterShould {
    @Test
    fun `convert byte array to string`() {
        val body = randomAlphanumeric(20)
        assertThat(body.toByteArray().stringify(), `is`(body))
    }
}
