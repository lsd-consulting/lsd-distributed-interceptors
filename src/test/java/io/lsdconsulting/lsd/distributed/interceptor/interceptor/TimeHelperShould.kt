package io.lsdconsulting.lsd.distributed.interceptor.interceptor

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.greaterThanOrEqualTo
import org.hamcrest.Matchers.lessThanOrEqualTo
import org.junit.jupiter.api.Test

class TimeHelperShould {

    @Test
    fun `return value`() {
        val before = System.nanoTime()
        val result = TimeHelper.getNow()
        val after = System.nanoTime()
        assertThat(result, greaterThanOrEqualTo(before))
        assertThat(result, lessThanOrEqualTo(after))
    }
}
