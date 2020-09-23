package com.integreety.yatspec.e2e.captor.http.mapper.source;

import lombok.RequiredArgsConstructor;
import lombok.Value;

import java.util.Map;

import static java.util.Comparator.reverseOrder;

@Value
@RequiredArgsConstructor
public class UserSuppliedSourceMappings implements SourceNameMappings {

    Map<String, String> mappings;
    SourceNameMappings fallbackMapper;

    @Override
    public String mapForPath(final String path) {
        final String nameKey = mappings.keySet().stream()
                .sorted(reverseOrder())
                .filter(path::startsWith)
                .findFirst()
                .orElse("default");

        return mappings.getOrDefault(nameKey, fallbackMapper.mapForPath(path));
    }

    public static SourceNameMappings userSuppliedSourceMappings(final Map<String, String> mappings) {
        return new UserSuppliedSourceMappings(mappings, ALWAYS_APP);
    }
}
