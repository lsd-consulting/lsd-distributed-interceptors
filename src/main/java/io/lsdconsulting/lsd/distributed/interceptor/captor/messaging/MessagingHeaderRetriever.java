package io.lsdconsulting.lsd.distributed.interceptor.captor.messaging;

import io.lsdconsulting.lsd.distributed.interceptor.captor.header.Obfuscator;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toMap;

@RequiredArgsConstructor
public class MessagingHeaderRetriever {

    private final Obfuscator obfuscator;

    public Map<String, Collection<String>> retrieve(final Message<?> message) {
        return obfuscator.obfuscate(message.getHeaders().entrySet().stream()
                .collect(toMap(Map.Entry::getKey, e -> e.getValue() != null ? List.of(e.getValue().toString()) : emptyList())));
    }
}
