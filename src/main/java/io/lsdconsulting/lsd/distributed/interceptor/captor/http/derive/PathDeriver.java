package io.lsdconsulting.lsd.distributed.interceptor.captor.http.derive;

import org.springframework.http.HttpRequest;

public class PathDeriver {
    private static final String EXTRACT_PATH = "https?://.*?(/.*)";

    public String derivePathFrom(final String url) {
        String path = url.replaceAll(EXTRACT_PATH, "$1");
        if (path.equals(url)) {
            return "";
        }
        return path;
    }

    public String derivePathFrom(final HttpRequest request) {
        return request.getURI().getPath() + (request.getURI().getQuery() != null ? "?" + request.getURI().getQuery() : "");
    }
}
