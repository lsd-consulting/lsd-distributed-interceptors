package com.integreety.yatspec.e2e.captor.http.mapper;

import lombok.RequiredArgsConstructor;
import lombok.Value;

import java.util.Map;

import static java.util.Comparator.reverseOrder;

@Value
@RequiredArgsConstructor
public class UserSuppliedDestinationMappings implements DestinationNameMappings {

    Map<String, String> mappings;
    DestinationNameMappings fallbackMapper;

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