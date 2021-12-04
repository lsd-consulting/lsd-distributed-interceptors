package io.lsdconsulting.lsd.distributed.interceptor.captor.http.derive;


import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.http.HttpRequest;

import java.net.URI;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

class PathDeriverShould {

    private final PathDeriver underTest = new PathDeriver() {};

    @ParameterizedTest
    @CsvSource(value = {
            "http://www.bbc.co.uk/somePage.html?abc=def, /somePage.html?abc=def",
            "http://www.bbc.co.uk/somePage.html, /somePage.html",
            "https://www.bbc.co.uk/customer/1/address, /customer/1/address"
    })
    void derivePathFrom(final String url, final String expectedPath) {
        final String path = underTest.derivePathFrom(url);
        assertThat(path, is(expectedPath));
    }

    @Test
    void derivePathFromHttpRequest() {
        HttpRequest httpRequest = mock(HttpRequest.class);
        given(httpRequest.getURI()).willReturn(URI.create("https://localhost.com/resource/childResource?param=value"));

        final String path = underTest.derivePathFrom(httpRequest);

        assertThat(path, is("/resource/childResource?param=value"));
    }
}