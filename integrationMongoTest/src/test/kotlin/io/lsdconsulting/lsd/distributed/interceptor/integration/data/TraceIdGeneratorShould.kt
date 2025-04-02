package io.lsdconsulting.lsd.distributed.interceptor.integration.data

import io.lsdconsulting.lsd.distributed.interceptor.integration.data.TraceIdGenerator.generate
import org.apache.commons.lang3.RandomUtils.secure
import org.hamcrest.CoreMatchers
import org.hamcrest.MatcherAssert
import org.junit.jupiter.api.Test

internal class TraceIdGeneratorShould {
    @Test
    fun `generate trace id of correct length`() {
        for (count in 0 until secure().randomInt(1000, 2000)) {
            MatcherAssert.assertThat(generate().length, CoreMatchers.`is`(16))
        }
    }

    @Test
    fun `generate hexadecimal value`() {
        for (count in 0 until secure().randomInt(1000, 2000)) {
            generate().toLong(16)
        }
    }
}
