package com.yatspec.e2e.captor.rabbit.header;

import org.springframework.amqp.core.MessageProperties;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class HeaderRetriever {

    public Map<String, Collection<String>> retrieve(final MessageProperties messageProperties) {
        return messageProperties.getHeaders().entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue() != null ? List.of(e.getValue().toString()) : Collections.emptyList()));
    }
}