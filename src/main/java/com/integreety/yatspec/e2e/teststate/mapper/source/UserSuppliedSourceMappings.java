package com.integreety.yatspec.e2e.teststate.mapper.source;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Map;

import static java.util.Comparator.reverseOrder;

@RequiredArgsConstructor
public class UserSuppliedSourceMappings implements SourceNameMappings {

    private final Map<Pair<String, String>, String> mappings;

    @Override
    public String mapFor(final Pair<String, String> pair) {
        final Pair<String, String> key = mappings.keySet().stream()
                .filter(p -> p.getLeft().equals(pair.getLeft()))
                .sorted(reverseOrder())
                .filter(p -> pair.getRight().startsWith(p.getRight()))
                .findFirst()
                .orElse(pair);
        return mappings.getOrDefault(key, pair.getLeft());
    }

    public static SourceNameMappings userSuppliedSourceMappings(final Map<Pair<String, String>, String> mappings) {
        return new UserSuppliedSourceMappings(mappings);
    }
}
