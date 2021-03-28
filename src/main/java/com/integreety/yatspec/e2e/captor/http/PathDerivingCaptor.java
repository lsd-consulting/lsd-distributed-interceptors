package com.integreety.yatspec.e2e.captor.http;

import org.springframework.http.HttpRequest;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;

public abstract class PathDerivingCaptor {
    static final String SOURCE_NAME_KEY = "Source-Name";
    static final String TARGET_NAME_KEY = "Target-Name";

    private static final String EXTRACT_PATH = "https?://.*?(/.*)";

    String derivePath(final String url) {
        return url.replaceAll(EXTRACT_PATH, "$1");
    }

    String generatePath(final HttpRequest request) {
        return request.getURI().getPath() + "?" + request.getURI().getQuery();
    }

    Optional<String> findHeader(final Map<String, Collection<String>> headers, final String targetNameKey) {
        return headers.get(targetNameKey).stream().filter(Objects::nonNull).findFirst();
    }

    boolean headerExists(final Map<String, Collection<String>> headers, final String targetNameKey) {
        return isNotEmpty(headers) && headers.containsKey(targetNameKey);
    }
}