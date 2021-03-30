package com.integreety.yatspec.e2e.captor.http.derive;

import org.springframework.http.HttpRequest;

public class PathDeriver {
    private static final String EXTRACT_PATH = "https?://.*?(/.*)";

    public String derivePathFrom(final String url) {
        return url.replaceAll(EXTRACT_PATH, "$1");
    }

    public String derivePathFrom(final HttpRequest request) {
        return request.getURI().getPath() + "?" + request.getURI().getQuery();
    }
}
