package io.lsdconsulting.lsd.distributed.interceptor.captor.messaging

import io.lsdconsulting.lsd.distributed.interceptor.captor.common.Obfuscator
import io.mockk.every
import io.mockk.mockk
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.contains
import org.hamcrest.Matchers.hasSize
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.springframework.messaging.Message
import org.springframework.messaging.MessageHeaders
import java.util.*
import java.util.stream.Stream

internal class MessagingHeaderRetrieverShould {
    private val obfuscator = mockk<Obfuscator>()
    private val underTest = MessagingHeaderRetriever(obfuscator)

    @BeforeEach
    fun setup() {
        every { obfuscator.obfuscate(any()) } answers { firstArg() }
    }

    @ParameterizedTest
    @MethodSource("provideMessageHeaders")
    fun `retrieve message headers from messages`(messageHeaders: MessageHeaders, expectedSize: Int) {
        val message = mockk<Message<String>>()
        every { message.headers } returns messageHeaders

        val result = underTest.retrieve(message)

        assertThat<Set<String>>(result.keys, hasSize(expectedSize))
        messageHeaders.keys.forEach {
            assertThat(result[it], contains(messageHeaders[it].toString()))
        }
    }

    companion object {
        @JvmStatic
        private fun provideMessageHeaders(): Stream<Arguments> {
            val mp1Value = MessageHeaders(mapOf<String, Any>("name" to "value"))
            val mp2Values = MessageHeaders(mapOf<String, Any>("name1" to "value1", "name2" to "value2"))
            return Stream.of(
                Arguments.of(mp1Value, 3),
                Arguments.of(mp2Values, 4),
                Arguments.of(MessageHeaders(mapOf()), 2)
            )
        }
    }
}
