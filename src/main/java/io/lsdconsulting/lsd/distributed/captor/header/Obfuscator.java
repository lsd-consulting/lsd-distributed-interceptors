package io.lsdconsulting.lsd.distributed.captor.header;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Arrays.stream;
import static java.util.Collections.emptyList;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

public class Obfuscator {
    public static final String DELIMINATOR = ",";

    private final List<String> sensitiveHeaders;

    public Obfuscator(String headers) {
        sensitiveHeaders = ofNullable(headers)
                .map(h -> stream(h.split(DELIMINATOR)).map(String::trim).collect(toList()))
                .orElse(emptyList());
    }

    public Map<String, Collection<String>> obfuscate(Map<String, Collection<String>> headers) {
        Map<String, List<String>> obfuscatedHeaders = headers.entrySet().stream()
                .filter(entry -> sensitiveHeaders.contains(entry.getKey()))
                .collect(toMap(Map.Entry::getKey, value -> List.of("<obfuscated>")));

        headers = new HashMap<>(headers);
        headers.putAll(obfuscatedHeaders);

        return headers;
    }
}
