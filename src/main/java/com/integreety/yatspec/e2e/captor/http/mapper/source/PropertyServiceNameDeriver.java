package com.integreety.yatspec.e2e.captor.http.mapper.source;

import lombok.Value;

@Value
public class PropertyServiceNameDeriver implements SourceNameMappings {

    String appName;

    @Override
    public String mapForPath(final String path) {
        return appName.replaceAll(" Service", "").replaceAll(" ", "");
    }
}
