package io.lsdconsulting.lsd.distributed.interceptor.captor.header;

import feign.Request;
import feign.Response;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.Message;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpResponse;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toMap;

@RequiredArgsConstructor
public class HeaderRetriever {

    private final Obfuscator obfuscator;

    public Map<String, Collection<String>> retrieve(final Message message) {
        return obfuscator.obfuscate(message.getMessageProperties().getHeaders().entrySet().stream()
                .collect(toMap(Map.Entry::getKey, e -> e.getValue() != null ? List.of(e.getValue().toString()) : emptyList())));
    }

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
