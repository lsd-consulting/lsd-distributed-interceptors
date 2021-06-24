package io.lsdconsulting.lsd.distributed.captor.rabbit.mapper;

import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.MessageProperties;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class ExchangeNameDeriverShould {

    private final ExchangeNameDeriver underTest = new ExchangeNameDeriver();

    @Test
    public void useTargetNameHeader() {
        final MessageProperties messageProperties = new MessageProperties();
        messageProperties.setHeader("Target-Name", "target");
        messageProperties.setHeader("__TypeId__", "typeId");

        final String result = underTest.derive(messageProperties, "alternative");

        assertThat(result, is("target"));
    }

    @Test
    public void useTypeIdHeader() {
        final MessageProperties messageProperties = new MessageProperties();
        messageProperties.setHeader("__TypeId__", "typeId");

        final String result = underTest.derive(messageProperties, "alternative");

        assertThat(result, is("typeId"));
    }

    @Test
    public void useAlternativeValue() {
        final MessageProperties messageProperties = new MessageProperties();

        final String result = underTest.derive(messageProperties, "alternative");

        assertThat(result, is("alternative"));
    }

    @Test
    public void useUnknownEvent() {
        final MessageProperties messageProperties = new MessageProperties();

        final String result = underTest.derive(messageProperties, "");

        assertThat(result, is("UNKNOWN_EVENT"));
    }
}