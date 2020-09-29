package com.integreety.yatspec.e2e.teststate.mapper.destination;

import org.junit.jupiter.api.Test;

import static java.util.Map.of;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

class UserSuppliedDestinationMappingsShould {

    @Test
    void usesPrefixToMatchNameToPath() {
        final DestinationNameMappings names = UserSuppliedDestinationMappings.userSuppliedDestinationMappings(of("/name", "NamingService"));

        assertThat(names.mapForPath("/name/one"), equalTo("NamingService"));
    }

    @Test
    void usesDefaultKeyIfNoMatch() {
        final DestinationNameMappings names = UserSuppliedDestinationMappings.userSuppliedDestinationMappings(of("default", "SomeService"));

        assertThat(names.mapForPath("/name/one"), equalTo("SomeService"));
    }

    @Test
    void fallsBackToUsingPathPrefixForNameIfNoMatchAndNoDefault() {
        final DestinationNameMappings names = UserSuppliedDestinationMappings.userSuppliedDestinationMappings(of());

        assertThat(names.mapForPath("/service-name/something"), equalTo("service_name"));
    }

    @Test
    void picksMostSpecificMatch() {
        final DestinationNameMappings mappings = UserSuppliedDestinationMappings.userSuppliedDestinationMappings(of(
                "/na", "FirstNamingService",
                "/name/one", "MostSpecificNamingService",
                "/name/one/other", "DifferentNamingService",
                "/name", "MoreSpecificNamingService"
        ));

        assertThat(mappings.mapForPath("/name/one"), equalTo("MostSpecificNamingService"));
    }
}