package io.lsdconsulting.lsd.distributed.interceptor.captor.rabbit;

import io.lsdconsulting.lsd.distributed.interceptor.captor.header.Obfuscator;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.Message;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toMap;

@RequiredArgsConstructor
public class AmqpHeaderRetriever {

    private final Obfuscator obfuscator;

    public Map<String, Collection<String>> retrieve(final Message message) {
        return obfuscator.obfuscate(message.getMessageProperties().getHeaders().entrySet().stream()
                .collect(toMap(Map.Entry::getKey, e -> e.getValue() != null ? List.of(e.getValue().toString()) : emptyList())));
    }
}
