package io.lsdconsulting.lsd.distributed.interceptor.integration.data

import io.lsdconsulting.lsd.distributed.interceptor.integration.data.TraceIdGenerator.generate
import org.apache.commons.lang3.RandomUtils
import org.hamcrest.CoreMatchers
import org.hamcrest.MatcherAssert
import org.junit.jupiter.api.Test

class TraceIdGeneratorShould {
    @Test
    fun generateTraceIdOfCorrectLength() {
        for (count in 0 until RandomUtils.nextInt(1000, 2000)) {
            MatcherAssert.assertThat(generate().length, CoreMatchers.`is`(16))
        }
    }

    @Test
    fun generateHexadecimalValue() {
        for (count in 0 until RandomUtils.nextInt(1000, 2000)) {
            generate().toLong(16)
        }
    }
}
