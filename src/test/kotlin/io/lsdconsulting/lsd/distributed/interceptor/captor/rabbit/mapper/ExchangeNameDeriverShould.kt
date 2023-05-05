package io.lsdconsulting.lsd.distributed.interceptor.captor.rabbit.mapper

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.junit.jupiter.api.Test
import org.springframework.amqp.core.MessageProperties

internal class ExchangeNameDeriverShould {
    private val underTest = ExchangeNameDeriver()

    @Test
    fun `return target name header`() {
        val messageProperties = MessageProperties()
        messageProperties.setHeader("Target-Name", "target")
        messageProperties.setHeader("__TypeId__", "typeId")

        val result = underTest.derive(messageProperties, "alternative")
        assertThat(result, `is`("target"))
    }

    @Test
    fun `return type id header without packages`() {
        val messageProperties = MessageProperties()
        messageProperties.setHeader("__TypeId__", "ExchangeNameDeriver")

        val result = underTest.derive(messageProperties, "alternative")

        assertThat(result, `is`("ExchangeNameDeriver"))
    }

    @Test
    fun `return type id header with packages`() {
        val messageProperties = MessageProperties()
        messageProperties.setHeader("__TypeId__", "io.lsdconsulting.lsd.distributed.interceptor.captor.rabbit.mapper.ExchangeNameDeriver")

        val result = underTest.derive(messageProperties, "alternative")

        assertThat(result, `is`("ExchangeNameDeriver"))
    }

    @Test
    fun `return alternative value`() {
        val messageProperties = MessageProperties()

        val result = underTest.derive(messageProperties, "alternative")

        assertThat(result, `is`("alternative"))
    }

    @Test
    fun `return unknown event`() {
        val messageProperties = MessageProperties()

        val result = underTest.derive(messageProperties, "")

        assertThat(result, `is`("UNKNOWN_EVENT"))
    }

    @Test
    fun `handle null alternative exchange name`() {
        val messageProperties = MessageProperties()

        val result = underTest.derive(messageProperties, null)

        assertThat(result, `is`("UNKNOWN_EVENT"))
    }

    @Test
    fun `handle empty type id header`() {
        val messageProperties = MessageProperties()
        messageProperties.setHeader("__TypeId__", "")

        val result = underTest.derive(messageProperties, "alternative")

        assertThat(result, `is`("alternative"))
    }

    @Test
    fun `handle spaces only in type id header`() {
        val messageProperties = MessageProperties()
        messageProperties.setHeader("__TypeId__", "  ")

        val result = underTest.derive(messageProperties, "alternative")

        assertThat(result, `is`("alternative"))
    }

    @Test
    fun `handle null type id header`() {
        val messageProperties = MessageProperties()
        messageProperties.setHeader("__TypeId__", null)

        val result = underTest.derive(messageProperties, "alternative")

        assertThat(result, `is`("alternative"))
    }
}
