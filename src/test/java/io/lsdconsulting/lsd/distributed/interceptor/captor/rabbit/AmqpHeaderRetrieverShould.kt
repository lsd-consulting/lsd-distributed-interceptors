package io.lsdconsulting.lsd.distributed.interceptor.captor.rabbit

import io.lsdconsulting.lsd.distributed.interceptor.captor.header.Obfuscator
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
    fun retrieveHeadersFromMessages(messageProperties: MessageProperties, expectedSize: Int) {
        val message = mockk<Message>()
        every { message.messageProperties } returns messageProperties

        val result = underTest.retrieve(message)

        assertThat<Set<String>>(result.keys, hasSize(expectedSize))
        for (headerName in messageProperties.headers.keys) {
            val header = messageProperties.getHeader<String>(headerName)
            assertThat(result[headerName], contains(header))
        }
    }

    @Test
    fun handleHeadersWithNoValuesFromMessage() {
        val messageProperties = MessageProperties()
        messageProperties.setHeader("name", null)
        val message = mockk<Message>()
        every { message.messageProperties } returns messageProperties
        val headers = underTest.retrieve(message)

        assertThat<Set<String>>(headers.keys, hasSize(1))
        assertThat(headers["name"], `is`(listOf<Any>()))
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
