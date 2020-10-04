package com.integreety.yatspec.e2e.teststate.mapper.source;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.tuple.Pair;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static java.util.Comparator.reverseOrder;

@RequiredArgsConstructor
public class UserSuppliedSourceMappings implements SourceNameMappings {

    private final Map<Pair<String, String>, String> mappings;
    private final Set<Pair<String, String>> usedMappings = new HashSet<>();

    @Override
    public String mapFor(final Pair<String, String> pair) {
        final Pair<String, String> key = mappings.keySet().stream()
                .filter(p -> p.getLeft().equals(pair.getLeft()))
                .sorted(reverseOrder())
                .filter(p -> pair.getRight().startsWith(p.getRight()))
                .findFirst()
                .orElse(pair);

        if (key.equals(pair)) {
            usedMappings.add(key);
        }
        return mappings.getOrDefault(key, pair.getLeft());
    }

    @Override
    public Map<Pair<String, String>, String> getUnusedMappings() {
        final HashMap<Pair<String, String>, String> unusedMappings = new HashMap<>(mappings);
        for (final Pair<String, String> key: usedMappings) {
            unusedMappings.remove(key);
        }
        return unusedMappings;
    }

    public static SourceNameMappings userSuppliedSourceMappings(final Map<Pair<String, String>, String> mappings) {
        return new UserSuppliedSourceMappings(mappings);
    }
}
