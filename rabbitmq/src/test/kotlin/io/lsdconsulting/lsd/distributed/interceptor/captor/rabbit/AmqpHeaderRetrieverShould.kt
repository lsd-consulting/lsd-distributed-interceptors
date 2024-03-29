package io.lsdconsulting.lsd.distributed.interceptor.captor.rabbit

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
import org.springframework.amqp.core.Message
import org.springframework.amqp.core.MessageProperties
import java.util.stream.Stream

internal class AmqpHeaderRetrieverShould {
    private val obfuscator = mockk<Obfuscator>()
    private val underTest = AmqpHeaderRetriever(obfuscator)

    @BeforeEach
    fun setup() {
        every { obfuscator.obfuscate(any()) } answers { firstArg() }
    }

    @ParameterizedTest
    @MethodSource("provideMessageProperties")
    fun `retrieve headers from messages`(messageProperties: MessageProperties, expectedSize: Int) {
        val message = mockk<Message>()
        every { message.messageProperties } returns messageProperties

        val result = underTest.retrieve(message)

        assertThat(result.keys, hasSize(expectedSize))
        for (headerName in messageProperties.headers.keys) {
            val header = messageProperties.getHeader<String>(headerName)
            assertThat(result[headerName], contains(header))
        }
    }

    @Test
    fun `handle headers with no values from message`() {
        val messageProperties = MessageProperties()
        messageProperties.setHeader("name", null)
        val message = mockk<Message>()
        every { message.messageProperties } returns messageProperties

        val result = underTest.retrieve(message)

        assertThat(result.keys, hasSize(1))
        assertThat(result["name"], `is`(listOf<Any>()))
    }

    @Test
    fun `retrieve message headers with list value`() {
        val messageProperties = MessageProperties()
        messageProperties.setHeader("name", listOf("value"))
        val message = mockk<Message>()
        every { message.messageProperties } returns messageProperties

        val result = underTest.retrieve(message)

        assertThat(result.keys, hasSize(1))
        assertThat(result["name"], hasItem(containsString("value")))
    }

    @Test
    fun `retrieve message headers with map value`() {
        val messageProperties = MessageProperties()
        messageProperties.setHeader("name", mapOf("key" to "value"))
        val message = mockk<Message>()
        every { message.messageProperties } returns messageProperties

        val result = underTest.retrieve(message)

        assertThat(result.keys, hasSize(1))
        assertThat(result["name"], hasItem(containsString("key")))
        assertThat(result["name"], hasItem(containsString("value")))
    }

    companion object {
        @JvmStatic
        private fun provideMessageProperties(): Stream<Arguments> {
            val mp1Value = MessageProperties()
            mp1Value.setHeader("name", "value")
            val mp2Values = MessageProperties()
            mp2Values.setHeader("name1", "value1")
            mp2Values.setHeader("name2", "value2")
            return Stream.of(
                Arguments.of(mp1Value, 1),
                Arguments.of(mp2Values, 2),
                Arguments.of(MessageProperties(), 0)
            )
        }
    }
}
