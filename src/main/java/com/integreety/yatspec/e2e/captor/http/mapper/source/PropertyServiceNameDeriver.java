package com.integreety.yatspec.e2e.captor.http.mapper.source;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class PropertyServiceNameDeriver implements SourceNameMappings {

    final String appName;

    @Override
    public String mapForPath(final String path) {
        return appName.replaceAll(" Service", "").replaceAll(" ", "");
    }
}
