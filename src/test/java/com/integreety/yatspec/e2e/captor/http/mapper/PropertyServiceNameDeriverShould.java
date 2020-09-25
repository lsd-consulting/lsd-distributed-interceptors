package com.integreety.yatspec.e2e.captor.http.mapper;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class PropertyServiceNameDeriverShould {

    @ParameterizedTest
    @CsvSource(value = {
            "Global User Service, GlobalUser",
            "User Address, UserAddress"
    })
    public void deriveServiceName(final String appName, final String expectedServiceName) {
        final PropertyServiceNameDeriver underTest = new PropertyServiceNameDeriver(appName);
        assertThat(underTest.getServiceName(), is(expectedServiceName));
    }
}