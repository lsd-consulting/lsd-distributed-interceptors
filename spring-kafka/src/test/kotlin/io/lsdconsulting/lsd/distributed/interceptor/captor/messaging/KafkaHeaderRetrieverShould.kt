package io.lsdconsulting.lsd.distributed.interceptor.captor.messaging

import io.lsdconsulting.lsd.distributed.interceptor.captor.common.Obfuscator
import io.mockk.every
import io.mockk.mockk
import org.apache.kafka.common.header.Headers
import org.apache.kafka.common.header.internals.RecordHeader
import org.apache.kafka.common.header.internals.RecordHeaders
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.contains
import org.hamcrest.Matchers.hasSize
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.*
import java.util.stream.Stream

internal class KafkaHeaderRetrieverShould {
    private val obfuscator = mockk<Obfuscator>()
    private val underTest = KafkaHeaderRetriever(obfuscator)

    @BeforeEach
    fun setup() {
        every { obfuscator.obfuscate(any()) } answers { firstArg() }
    }

    @ParameterizedTest
    @MethodSource("provideMessageHeaders")
    fun `retrieve message headers from messages`(headers: Headers) {
        val result = underTest.retrieve(headers)

        assertThat(result.keys, hasSize(headers.count()))
        headers.toArray().forEach {
            assertThat(result[it.key()], contains(String(headers.headers(it.key()).first().value())))
        }
    }

    companion object {
        @JvmStatic
        private fun provideMessageHeaders(): Stream<Arguments> {
            val mp1Value = RecordHeaders(arrayOf(RecordHeader("name", "value".toByteArray())))
            val mp2Values = RecordHeaders(arrayOf(RecordHeader("name1", "value1".toByteArray()), RecordHeader("name2", "value2".toByteArray())))
            return Stream.of(
                Arguments.of(mp1Value),
                Arguments.of(mp2Values),
                Arguments.of(RecordHeaders(arrayOf<RecordHeader>()))
            )
        }
    }
}
