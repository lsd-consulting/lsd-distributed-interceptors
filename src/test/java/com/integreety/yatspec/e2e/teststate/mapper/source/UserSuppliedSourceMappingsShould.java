package com.integreety.yatspec.e2e.teststate.mapper.source;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Test;

import static java.util.Map.of;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

class UserSuppliedSourceMappingsShould {

    @Test
    void usePrefixToMatchNameToPath() {
        final SourceNameMappings names = UserSuppliedSourceMappings.userSuppliedSourceMappings(of(Pair.of("ServiceA", "/name"), "ServiceB"));

        assertThat(names.mapFor(Pair.of("ServiceA", "/name")), equalTo("ServiceB"));
    }

    @Test
    void useOriginalServiceNameIfTargetNoMatched() {
        final SourceNameMappings names = UserSuppliedSourceMappings.userSuppliedSourceMappings(of(Pair.of("ServiceA", "/name"), "ServiceB"));

        assertThat(names.mapFor(Pair.of("ServiceA", "/notMapped")), equalTo("ServiceA"));
    }

    @Test
    void useOriginalServiceNameIfNothingMatches() {
        final SourceNameMappings names = UserSuppliedSourceMappings.userSuppliedSourceMappings(of());

        assertThat(names.mapFor(Pair.of("ServiceA", "/notMapped")), equalTo("ServiceA"));
    }

    @Test
    void pickMostSpecificMatch() {
        final SourceNameMappings mappings = UserSuppliedSourceMappings.userSuppliedSourceMappings(of(
                Pair.of("ServiceA", "/na"), "User",
                Pair.of("ServiceA", "/name/one"), "Consumer",
                Pair.of("ServiceA", "/name/one/other"), "Client",
                Pair.of("ServiceA", "/name"), "Admin"
        ));

        assertThat(mappings.mapFor(Pair.of("ServiceA", "/name/one")), equalTo("Consumer"));
    }

    @Test
    void findsUnusedMappingsWhenMatchingLessSpecificPath() {
        final SourceNameMappings mappings = UserSuppliedSourceMappings.userSuppliedSourceMappings(of(
                Pair.of("ServiceA", "/na"), "User",
                Pair.of("ServiceA", "/name/one"), "Consumer",
                Pair.of("ServiceA", "/name/one/other"), "Client",
                Pair.of("ServiceA", "/name"), "Admin"
        ));

        mappings.mapFor(Pair.of("ServiceB", "/name/one"));
        mappings.mapFor(Pair.of("ServiceA", "/name/one"));
        mappings.mapFor(Pair.of("ServiceA", "/na"));
        assertThat(mappings.getUnusedMappings().keySet(), hasSize(2));
        assertThat(mappings.getUnusedMappings(), hasEntry(Pair.of("ServiceA", "/name/one/other"), "Client"));
        assertThat(mappings.getUnusedMappings(), hasEntry(Pair.of("ServiceA", "/name"), "Admin"));
    }

    @Test
    void findsUnusedMappingsWhenMatchingMoreSpecificPath() {
        final SourceNameMappings mappings = UserSuppliedSourceMappings.userSuppliedSourceMappings(of(
                Pair.of("ServiceA", "/name/one"), "Consumer",
                Pair.of("ServiceA", "/name/one/other"), "Client"
        ));

        mappings.mapFor(Pair.of("ServiceA", "/name/one/two"));
        assertThat(mappings.getUnusedMappings().keySet(), hasSize(1));
        assertThat(mappings.getUnusedMappings(), hasEntry(Pair.of("ServiceA", "/name/one/other"), "Client"));
    }
}