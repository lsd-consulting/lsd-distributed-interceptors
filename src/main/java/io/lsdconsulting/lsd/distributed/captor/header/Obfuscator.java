package io.lsdconsulting.lsd.distributed.captor.header;

import java.util.*;

import static java.util.stream.Collectors.toMap;

public class Obfuscator {

    private final List<String> sensitiveHeaders = new ArrayList<>();

    public Obfuscator() {
        sensitiveHeaders.add("Authorization");
        sensitiveHeaders.add("JWT");
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
