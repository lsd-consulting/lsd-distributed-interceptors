package com.integreety.yatspec.e2e.teststate.mapper.destination;

import java.util.Map;

public interface DestinationNameMappings {
    String mapForPath(String path);
    Map<String, String> getUnusedMappings();
}