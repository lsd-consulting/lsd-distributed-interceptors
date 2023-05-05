package io.lsdconsulting.lsd.distributed.interceptor.captor.rabbit.mapper

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.junit.jupiter.api.Test
import org.springframework.amqp.core.MessageProperties

internal class ExchangeNameDeriverShould {
    private val underTest = ExchangeNameDeriver()

    @Test
    fun useTargetNameHeader() {
        val messageProperties = MessageProperties()
        messageProperties.setHeader("Target-Name", "target")
        messageProperties.setHeader("__TypeId__", "typeId")

        val result = underTest.derive(messageProperties, "alternative")
        assertThat(result, `is`("target"))
    }

    @Test
    fun useTypeIdHeader() {
        val messageProperties = MessageProperties()
        messageProperties.setHeader("__TypeId__", "typeId")

        val result = underTest.derive(messageProperties, "alternative")

        assertThat(result, `is`("typeId"))
    }

    @Test
    fun useAlternativeValue() {
        val messageProperties = MessageProperties()

        val result = underTest.derive(messageProperties, "alternative")

        assertThat(result, `is`("alternative"))
    }

    @Test
    fun useUnknownEvent() {
        val messageProperties = MessageProperties()

        val result = underTest.derive(messageProperties, "")

        assertThat(result, `is`("UNKNOWN_EVENT"))
    }
}
