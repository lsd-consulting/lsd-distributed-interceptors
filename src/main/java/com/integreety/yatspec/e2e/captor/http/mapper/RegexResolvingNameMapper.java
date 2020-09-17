package com.integreety.yatspec.e2e.captor.http.mapper;

import lombok.Value;

@Value
public class RegexResolvingNameMapper implements DestinationNameMappings {

    private static final String FIRST_PART_OF_PATH = "^/?(.*?)([/?].*|$)";
    private static final String PLANT_UML_CRYPTONITE = "[-]"; //Characters that blow up PlantUml need to be replaced

    @Override
    public String mapForPath(final String path) {
        return path
                .replaceFirst(FIRST_PART_OF_PATH, "$1")
                .replaceAll(PLANT_UML_CRYPTONITE, "_");
    }
}
