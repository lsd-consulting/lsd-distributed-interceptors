package io.lsdconsulting.lsd.distributed.interceptor.captor.messaging;

import io.lsdconsulting.lsd.distributed.interceptor.captor.header.Obfuscator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.messaging.MessageHeaders;

import java.util.Collection;
import java.util.Map;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class MessagingHeaderRetrieverShould {
    private final Obfuscator obfuscator = mock(Obfuscator.class);

    private final MessagingHeaderRetriever underTest = new MessagingHeaderRetriever(obfuscator);

    @BeforeEach
    void setup() {
        when(obfuscator.obfuscate(any())).then(returnsFirstArg());
    }

    @ParameterizedTest
    @MethodSource("provideMessageHeaders")
    void retrieveMessageHeadersFromMessages(MessageHeaders messageHeaders, int expectedSize) {
        final org.springframework.messaging.Message<?> message = mock(org.springframework.messaging.Message.class);
        given(message.getHeaders()).willReturn(messageHeaders);

        final Map<String, Collection<String>> result = underTest.retrieve(message);

        assertThat(result.keySet(), hasSize(expectedSize));
        for (String headerName : messageHeaders.keySet()) {
            assertThat(result.get(headerName), contains(requireNonNull(messageHeaders.get(headerName)).toString()));
        }
    }

    private static Stream<Arguments> provideMessageHeaders() {
        MessageHeaders mp1Value = new MessageHeaders(Map.of("name", "value"));
        MessageHeaders mp2Values = new MessageHeaders(Map.of("name1", "value1", "name2", "value2"));
        return Stream.of(
                Arguments.of(mp1Value, 3),
                Arguments.of(mp2Values, 4),
                Arguments.of(new MessageHeaders(Map.of()), 2)
        );
    }
}