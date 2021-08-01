package io.lsdconsulting.lsd.distributed.captor.header;

import feign.Request;
import feign.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.util.MultiValueMapAdapter;

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

class HeaderRetrieverShould {
    private final Obfuscator obfuscator = mock(Obfuscator.class);

    private final HeaderRetriever underTest = new HeaderRetriever(obfuscator);

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

    @ParameterizedTest
    @MethodSource("provideHttpHeaders")
    void retrieveHeadersFromHttpRequest(HttpHeaders headers, int expectedSize) {
        final HttpRequest httpRequest = mock(HttpRequest.class);
        given(httpRequest.getHeaders()).willReturn(headers);

        final Map<String, Collection<String>> result = underTest.retrieve(httpRequest);

        assertThat(result.keySet(), hasSize(expectedSize));
        for (String headerName : headers.keySet()) {
            assertThat(result.get(headerName), is(headers.get(headerName)));
        }
    }

    @ParameterizedTest
    @MethodSource("provideHttpHeaders")
    void retrieveHeadersFromHttpResponse(HttpHeaders headers, int expectedSize) {
        final ClientHttpResponse httpResponse = mock(ClientHttpResponse.class);
        given(httpResponse.getHeaders()).willReturn(headers);

        final Map<String, Collection<String>> result = underTest.retrieve(httpResponse);

        assertThat(result.keySet(), hasSize(expectedSize));
        for (String headerName : headers.keySet()) {
            assertThat(result.get(headerName), is(headers.get(headerName)));
        }
    }

    private static Stream<Arguments> provideHttpHeaders() {
        return Stream.of(
                Arguments.of(new HttpHeaders(new MultiValueMapAdapter<>(Map.of("name", List.of("value")))), 1),
                Arguments.of(new HttpHeaders(new MultiValueMapAdapter<>(Map.of("name1", List.of("value1"), "name2", List.of("value2")))), 2),
                Arguments.of(new HttpHeaders(new MultiValueMapAdapter<>(Map.of())), 0)
        );
    }

    @ParameterizedTest
    @MethodSource("provideHeaders")
    void retrieveHeadersFromRequest(Map<String, Collection<String>> headers, int expectedSize) {
        final Request request = mock(Request.class);
        given(request.headers()).willReturn(headers);

        final Map<String, Collection<String>> result = underTest.retrieve(request);

        assertThat(result.keySet(), hasSize(expectedSize));
        for (String headerName : headers.keySet()) {
            assertThat(result.get(headerName), is(headers.get(headerName)));
        }
    }

    @ParameterizedTest
    @MethodSource("provideHeaders")
    void retrieveHeadersFromResponse(Map<String, Collection<String>> headers, int expectedSize) {
        final Response response = mock(Response.class);
        given(response.headers()).willReturn(headers);

        final Map<String, Collection<String>> result = underTest.retrieve(response);

        assertThat(result.keySet(), hasSize(expectedSize));
        for (String headerName : headers.keySet()) {
            assertThat(result.get(headerName), is(headers.get(headerName)));
        }
    }

    private static Stream<Arguments> provideHeaders() {
        return Stream.of(
                Arguments.of(Map.of("name", List.of("value")), 1),
                Arguments.of(Map.of("name1", List.of("value1"), "name2", List.of("value2")), 2),
                Arguments.of(Map.of(), 0)
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

    @Test
    public void handleHeadersWithNoValuesFromHttpRequest() {
        HttpHeaders headers = new HttpHeaders(new MultiValueMapAdapter<>(Map.of("name", List.of())));
        final HttpRequest httpRequest = mock(HttpRequest.class);
        given(httpRequest.getHeaders()).willReturn(headers);

        final Map<String, Collection<String>> result = underTest.retrieve(httpRequest);

        assertThat(result.keySet(), hasSize(1));
        assertThat(result.get("name"), is(List.of()));
    }

    @Test
    public void handleHeadersWithNoValuesFromHttpResponse() {
        HttpHeaders headers = new HttpHeaders(new MultiValueMapAdapter<>(Map.of("name", List.of())));
        final ClientHttpResponse httpResponse = mock(ClientHttpResponse.class);
        given(httpResponse.getHeaders()).willReturn(headers);

        final Map<String, Collection<String>> result = underTest.retrieve(httpResponse);

        assertThat(result.keySet(), hasSize(1));
        assertThat(result.get("name"), is(List.of()));
    }
}