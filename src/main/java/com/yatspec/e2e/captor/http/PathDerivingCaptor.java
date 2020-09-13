package com.yatspec.e2e.captor.http;

public abstract class PathDerivingCaptor {
    private static final String EXTRACT_PATH = "https?://.*?(/.*)";

    protected String derivePath(final String url) {
        return url.replaceAll(EXTRACT_PATH, "$1");
    }
}