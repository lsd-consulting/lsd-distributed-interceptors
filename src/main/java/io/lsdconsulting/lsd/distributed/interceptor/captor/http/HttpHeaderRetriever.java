package io.lsdconsulting.lsd.distributed.interceptor.captor.http;

import feign.Request;
import feign.Response;
import io.lsdconsulting.lsd.distributed.interceptor.captor.header.Obfuscator;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpResponse;

import java.util.Collection;
import java.util.Map;

import static java.util.stream.Collectors.toMap;

@RequiredArgsConstructor
public class HttpHeaderRetriever {

    private final Obfuscator obfuscator;

    public Map<String, Collection<String>> retrieve(HttpRequest request) {
        return obfuscator.obfuscate(request.getHeaders().entrySet().stream().collect(toMap(Map.Entry::getKey, Map.Entry::getValue)));
    }

    public Map<String, Collection<String>> retrieve(ClientHttpResponse response) {
        return obfuscator.obfuscate(response.getHeaders().entrySet().stream().collect(toMap(Map.Entry::getKey, Map.Entry::getValue)));
    }

    public Map<String, Collection<String>> retrieve(Request request) {
        return obfuscator.obfuscate(request.headers());
    }

    public Map<String, Collection<String>> retrieve(Response response) {
        return obfuscator.obfuscate(response.headers());
    }
}
