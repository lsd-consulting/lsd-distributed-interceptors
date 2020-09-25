package com.integreety.yatspec.e2e.teststate.mapper.destination;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class RegexResolvingNameMapperShould {

    @ParameterizedTest
    @CsvSource(value = {
            "/pricing, pricing",
            "/pricing-service, pricing_service",
            "/pricing-service/add/123, pricing_service",
            "/pricing-service?id=123&type=live, pricing_service"
    })
    void resolveDestinationNameByPath(final String path, final String name) {
        final RegexResolvingNameMapper nameMapper = new RegexResolvingNameMapper();

        assertThat(nameMapper.mapForPath(path), is(name));
    }
}