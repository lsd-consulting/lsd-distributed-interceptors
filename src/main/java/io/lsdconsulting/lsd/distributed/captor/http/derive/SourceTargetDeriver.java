package io.lsdconsulting.lsd.distributed.captor.http.derive;

import lombok.RequiredArgsConstructor;

import javax.annotation.PostConstruct;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;

@RequiredArgsConstructor
public class SourceTargetDeriver {
    static final String SOURCE_NAME_KEY = "Source-Name";
    static final String TARGET_NAME_KEY = "Target-Name";
    static final String UNKNOWN_TARGET = "UNKNOWN_TARGET";

    private final PropertyServiceNameDeriver propertyServiceNameDeriver;
    private String serviceName;

    @PostConstruct
    public void initialise() {
        serviceName = propertyServiceNameDeriver.getServiceName();
    }

    public String deriveServiceName(final Map<String, Collection<String>> headers) {
        return headerExists(headers, SOURCE_NAME_KEY) ? findHeader(headers, SOURCE_NAME_KEY).orElse(serviceName) : serviceName;
    }

    public String deriveTarget(final Map<String, Collection<String>> headers, final String path) {
        return headerExists(headers, TARGET_NAME_KEY) ? findHeader(headers, TARGET_NAME_KEY).orElse(path != null ? path : UNKNOWN_TARGET) : UNKNOWN_TARGET;
    }

    private Optional<String> findHeader(final Map<String, Collection<String>> headers, final String targetNameKey) {
        return headers.get(targetNameKey).stream().filter(Objects::nonNull).findFirst();
    }

    private boolean headerExists(final Map<String, Collection<String>> headers, final String targetNameKey) {
        return isNotEmpty(headers) && headers.containsKey(targetNameKey);
    }
}
