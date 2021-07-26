package io.lsdconsulting.lsd.distributed.captor.header;

import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasEntry;

class ObfuscatorShould {

    private final Obfuscator underTest = new Obfuscator();

    @Test
    void obfuscateAuthorizationHeader() {
        Map<String, Collection<String>> headers = Map.of("Authorization", List.of(randomAlphanumeric(30)));

        Map<String, Collection<String>> result = underTest.obfuscate(headers);

        assertThat(result, hasEntry("Authorization", List.of("<obfuscated>")));
    }

    @Test
    void obfuscateJWTHeader() {
        Map<String, Collection<String>> headers = Map.of("JWT", List.of(randomAlphanumeric(30)));

        Map<String, Collection<String>> result = underTest.obfuscate(headers);

        assertThat(result, hasEntry("JWT", List.of("<obfuscated>")));
    }

    @Test
    void keepOtherHeadersUnchanged() {
        String headerName = randomAlphanumeric(30);
        String headerValue = randomAlphanumeric(30);
        Map<String, Collection<String>> headers = Map.of(headerName, List.of(headerValue));

        Map<String, Collection<String>> result = underTest.obfuscate(headers);

        assertThat(result, hasEntry(headerName, List.of(headerValue)));
    }
}
