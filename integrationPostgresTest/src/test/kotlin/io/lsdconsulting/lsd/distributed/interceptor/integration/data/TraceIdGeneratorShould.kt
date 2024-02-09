package io.lsdconsulting.lsd.distributed.interceptor.integration.data

import io.lsdconsulting.lsd.distributed.interceptor.integration.data.TraceIdGenerator.generate
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.jupiter.api.Test
import java.util.*

internal class TraceIdGeneratorShould {
    @Test
    fun `generate trace id of correct length`() {
        for (count in 0 until Random().nextInt(1000, 2000)) {
            assertThat(generate().length, `is`(16))
        }
    }

    @Test
    fun `generate hexadecimal value`() {
        for (count in 0 until Random().nextInt(1000, 2000)) {
            generate().toLong(16)
        }
    }
}
