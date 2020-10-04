package com.integreety.yatspec.e2e.teststate.mapper.destination;

import java.util.Map;

public class RegexResolvingNameMapper implements DestinationNameMappings {

    private static final String FIRST_PART_OF_PATH = "^/?(.*?)([/?].*|$)";
    private static final String PLANT_UML_CRYPTONITE = "[-]"; //Characters that blow up PlantUml need to be replaced

    @Override
    public String mapForPath(final String path) {
        return path
                .replaceFirst(FIRST_PART_OF_PATH, "$1")
                .replaceAll(PLANT_UML_CRYPTONITE, "_");
    }

    @Override
    public Map<String, String> getUnusedMappings() {
        return Map.of();
    }
}
