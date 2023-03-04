package io.lsdconsulting.lsd.distributed.interceptor.captor.http;

import feign.Request;
import feign.Response;
import io.lsdconsulting.lsd.distributed.interceptor.captor.header.Obfuscator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.util.MultiValueMapAdapter;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class HttpHeaderRetrieverShould {
    private final Obfuscator obfuscator = mock(Obfuscator.class);

    private final HttpHeaderRetriever underTest = new HttpHeaderRetriever(obfuscator);

    @BeforeEach
    void setup() {
        when(obfuscator.obfuscate(any())).then(returnsFirstArg());
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