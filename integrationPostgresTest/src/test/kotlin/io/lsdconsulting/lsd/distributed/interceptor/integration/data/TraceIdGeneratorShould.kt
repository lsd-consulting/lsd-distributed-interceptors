package io.lsdconsulting.lsd.distributed.interceptor.integration.data

import io.lsdconsulting.lsd.distributed.interceptor.integration.data.TraceIdGenerator.generate
import org.apache.commons.lang3.RandomUtils
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.jupiter.api.Test

internal class TraceIdGeneratorShould {
    @Test
    fun `generate trace id of correct length`() {
        for (count in 0 until RandomUtils.secure().randomInt(1000, 2000)) {
            assertThat(generate().length, `is`(16))
        }
    }

    @Test
    fun `generate hexadecimal value`() {
        for (count in 0 until RandomUtils.secure().randomInt(1000, 2000)) {
            generate().toLong(16)
        }
    }
}
