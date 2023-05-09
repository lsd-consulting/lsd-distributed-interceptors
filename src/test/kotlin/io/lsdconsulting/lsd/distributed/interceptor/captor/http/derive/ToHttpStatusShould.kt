package io.lsdconsulting.lsd.distributed.interceptor.captor.http.derive

import org.apache.commons.lang3.RandomUtils
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.startsWith
import org.junit.jupiter.api.Test

internal class ToHttpStatusShould {
    @Test
    fun handleUnknownStatusCode() {
        val result = RandomUtils.nextInt(1000, 10000).toHttpStatus()
        assertThat(result, startsWith("<unresolved status:"))
    }

    @Test
    fun handleKnownStatusCode() {
        val result = 200.toHttpStatus()
        assertThat(result, `is`("200 OK"))
    }
}
