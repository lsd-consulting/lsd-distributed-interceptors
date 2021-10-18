package io.lsdconsulting.lsd.distributed.interceptor.captor.http.derive;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.apache.commons.lang3.RandomStringUtils.randomAlphabetic;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

class SourceTargetDeriverShould {

    private final PropertyServiceNameDeriver propertyServiceNameDeriver = mock(PropertyServiceNameDeriver.class);

    private final SourceTargetDeriver underTest = new SourceTargetDeriver(propertyServiceNameDeriver);

    private final String appName = randomAlphabetic(20);
    private final String sourceName = randomAlphabetic(20);
    private final String targetName = randomAlphabetic(20);
    private final String path = randomAlphabetic(20);

    @BeforeEach
    public void setup() {
        given(propertyServiceNameDeriver.getServiceName()).willReturn(appName);
        underTest.initialise();
    }

    @Test
    void useHeaderForSourceName() {
        final String serviceName = underTest.deriveServiceName(Map.of("Source-Name", List.of(sourceName)));

        assertThat(serviceName, is(sourceName));
    }

    @Test
    void fallbackToPropertyServiceNameIfSourceHeaderMissingValue() {
        final String serviceName = underTest.deriveServiceName(Map.of("Source-Name", List.of()));

        assertThat(serviceName, is(appName));
    }

    @Test
    void fallbackToPropertyServiceNameWhenNoSourceHeader() {
        final String serviceName = underTest.deriveServiceName(Map.of());

        assertThat(serviceName, is(appName));
    }

    @Test
    void useHeaderForTargetName() {
        final String serviceName = underTest.deriveTarget(Map.of("Target-Name", List.of(targetName)), path);

        assertThat(serviceName, is(targetName));
    }

    @Test
    void fallbackToPathTargetHeaderMissingValue() {
        final String serviceName = underTest.deriveTarget(Map.of("Target-Name", List.of()), path);

        assertThat(serviceName, is(path));
    }

    @Test
    void fallbackToPathTargetIfTargetHeaderMissingValueAndNoPath() {
        final String serviceName = underTest.deriveTarget(Map.of("Target-Name", List.of()), null);

        assertThat(serviceName, is("UNKNOWN_TARGET"));
    }

    @Test
    void fallbackToUnknownWhenNoTargetHeader() {
        final String serviceName = underTest.deriveTarget(Map.of(), null);

        assertThat(serviceName, is("UNKNOWN_TARGET"));
    }
}
