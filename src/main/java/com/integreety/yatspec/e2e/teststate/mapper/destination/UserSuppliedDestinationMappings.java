package com.integreety.yatspec.e2e.teststate.mapper.destination;

import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static java.util.Comparator.reverseOrder;

@RequiredArgsConstructor
public class UserSuppliedDestinationMappings implements DestinationNameMappings {

    private static final String DEFAULT_NAME = "default";
    private final Map<String, String> mappings;
    private final DestinationNameMappings fallbackMapper;
    private final Set<String> usedMappings = new HashSet<>();

    @Override
    public String mapForPath(final String path) {
        final String nameKey = mappings.keySet().stream()
                .sorted(reverseOrder())
                .filter(path::startsWith)
                .findFirst()
                .orElse(DEFAULT_NAME);

        if (!nameKey.equals(DEFAULT_NAME)) {
            usedMappings.add(nameKey);
        }
        return mappings.getOrDefault(nameKey, fallbackMapper.mapForPath(path));
    }

    @Override
    public Map<String, String> getUnusedMappings() {
        final HashMap<String, String> unusedMappings = new HashMap<>(mappings);
        for (final String name: usedMappings) {
            unusedMappings.remove(name);
        }
        return unusedMappings;
    }

    public static DestinationNameMappings userSuppliedDestinationMappings(final Map<String, String> mappings) {
        return new UserSuppliedDestinationMappings(mappings, new RegexResolvingNameMapper());
    }
}