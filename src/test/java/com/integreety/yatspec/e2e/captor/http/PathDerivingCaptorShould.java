package com.integreety.yatspec.e2e.captor.http;


import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class PathDerivingCaptorShould {

    private final PathDerivingCaptor pathDerivingCaptor = new PathDerivingCaptor() {};

    @ParameterizedTest
    @CsvSource(value = {
            "http://www.bbc.co.uk/somePage.html?abc=def, /somePage.html?abc=def",
            "http://www.bbc.co.uk/somePage.html, /somePage.html",
            "https://www.bbc.co.uk/customer/1/address, /customer/1/address"
    })
    public void derivePath(final String url, final String expectedPath) {
        final String path = pathDerivingCaptor.derivePath(url);
        assertThat(path, is(expectedPath));
    }
}