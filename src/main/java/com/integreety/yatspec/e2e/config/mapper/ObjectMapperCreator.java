package com.integreety.yatspec.e2e.config.mapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import static com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES;
import static com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS;

public class ObjectMapperCreator {
    private final ObjectMapper objectMapper;

    public ObjectMapperCreator() {
        objectMapper = objectMapper();
    }

    private ObjectMapper objectMapper() {
        final ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.configure(WRITE_DATES_AS_TIMESTAMPS, false);
        mapper.configure(FAIL_ON_UNKNOWN_PROPERTIES, false);
        return mapper;
    }

    public ObjectMapper getObjectMapper() {
        return objectMapper;
    }
}
