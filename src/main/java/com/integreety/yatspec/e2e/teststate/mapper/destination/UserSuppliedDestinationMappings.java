package com.integreety.yatspec.e2e.teststate.mapper.destination;

import lombok.RequiredArgsConstructor;

import java.util.Map;

import static java.util.Comparator.reverseOrder;

@RequiredArgsConstructor
public class UserSuppliedDestinationMappings implements DestinationNameMappings {

    private final Map<String, String> mappings;
    private final DestinationNameMappings fallbackMapper;

    @Override
    public String mapForPath(final String path) {
        final String nameKey = mappings.keySet().stream()
                .sorted(reverseOrder())
                .filter(path::startsWith)
                .findFirst()
                .orElse("default");

        return mappings.getOrDefault(nameKey, fallbackMapper.mapForPath(path));
    }

    public static DestinationNameMappings userSuppliedDestinationMappings(final Map<String, String> mappings) {
        return new UserSuppliedDestinationMappings(mappings, new RegexResolvingNameMapper());
    }
}