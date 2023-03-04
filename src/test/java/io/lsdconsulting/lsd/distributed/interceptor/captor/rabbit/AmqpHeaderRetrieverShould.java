package io.lsdconsulting.lsd.distributed.interceptor.captor.rabbit;

import io.lsdconsulting.lsd.distributed.interceptor.captor.header.Obfuscator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AmqpHeaderRetrieverShould {
    private final Obfuscator obfuscator = mock(Obfuscator.class);

    private final AmqpHeaderRetriever underTest = new AmqpHeaderRetriever(obfuscator);

    @BeforeEach
    void setup() {
        when(obfuscator.obfuscate(any())).then(returnsFirstArg());
    }

    @ParameterizedTest
    @MethodSource("provideMessageProperties")
    void retrieveHeadersFromMessages(MessageProperties messageProperties, int expectedSize) {
        final Message message = mock(Message.class);
        given(message.getMessageProperties()).willReturn(messageProperties);

        final Map<String, Collection<String>> result = underTest.retrieve(message);

        assertThat(result.keySet(), hasSize(expectedSize));
        for (String headerName : messageProperties.getHeaders().keySet()) {
            String header = messageProperties.getHeader(headerName);
            assertThat(result.get(headerName), contains(header));
        }
    }

    private static Stream<Arguments> provideMessageProperties() {
        MessageProperties mp1Value = new MessageProperties();
        mp1Value.setHeader("name", "value");
        MessageProperties mp2Values = new MessageProperties();
        mp2Values.setHeader("name1", "value1");
        mp2Values.setHeader("name2", "value2");
        return Stream.of(
                Arguments.of(mp1Value, 1),
                Arguments.of(mp2Values, 2),
                Arguments.of(new MessageProperties(), 0)
        );
    }

    @Test
    public void handleHeadersWithNoValuesFromMessage() {
        final MessageProperties messageProperties = new MessageProperties();
        messageProperties.setHeader("name", null);
        final Message message = mock(Message.class);
        given(message.getMessageProperties()).willReturn(messageProperties);

        final Map<String, Collection<String>> headers = underTest.retrieve(message);

        assertThat(headers.keySet(), hasSize(1));
        assertThat(headers.get("name"), is(List.of()));
    }
}
