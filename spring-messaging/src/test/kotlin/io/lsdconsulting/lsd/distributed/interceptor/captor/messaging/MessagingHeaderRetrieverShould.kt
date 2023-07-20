package io.lsdconsulting.lsd.distributed.interceptor.captor.messaging

import io.lsdconsulting.lsd.distributed.interceptor.captor.common.Obfuscator
import io.mockk.every
import io.mockk.mockk
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
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
    fun `retrieve message headers from messages`(messageHeaders: MessageHeaders, map: Map<String, Any>) {
        val message = mockk<Message<String>>()
        every { message.headers } returns messageHeaders

        val result = underTest.retrieve(message)

        assertThat(result.keys, hasSize(map.size + 2))
        map.keys.forEach {
            assertThat(result[it], hasItem(messageHeaders[it]))
        }
    }

    companion object {
        @JvmStatic
        private fun provideMessageHeaders(): Stream<Arguments> {
            val map1 = mapOf<String, Any>("name" to "value")
            val mp1Value = MessageHeaders(map1)
            val map2 = mapOf<String, Any>("name1" to "value1", "name2" to "value2")
            val mp2Values = MessageHeaders(map2)
//            val map3 = mapOf("name1" to listOf("value1"), "name2" to mapOf("key2" to "value2"))
//            val mp3Values = MessageHeaders(map3)
            return Stream.of(
                Arguments.of(mp1Value, map1),
                Arguments.of(mp2Values, map2),
//                Arguments.of(mp3Values, map3),
                Arguments.of(MessageHeaders(mapOf()), mapOf<String, Any>())
            )
        }
    }

    @Test
    fun `retrieve message headers with list value`() {
        val message = mockk<Message<String>>()
        every { message.headers } returns MessageHeaders(mapOf("name" to listOf("value")))

        val result = underTest.retrieve(message)

        assertThat(result.keys, hasSize(3))
        assertThat(result["name"], hasItem(containsString("value")))
    }

    @Test
    fun `retrieve message headers with map value`() {
        val message = mockk<Message<String>>()
        every { message.headers } returns MessageHeaders(mapOf("name" to mapOf("key" to "value")))

        val result = underTest.retrieve(message)

        assertThat(result.keys, hasSize(3))
        assertThat(result["name"], hasItem(containsString("key")))
        assertThat(result["name"], hasItem(containsString("value")))
    }
}
