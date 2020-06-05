package com.jayfella.plugin.manager.json;

import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonObjectMapper {

    private static JsonObjectMapper INSTANCE;

    private final ObjectMapper objectMapper;

    private JsonObjectMapper() {
        objectMapper = new ObjectMapper();
    }

    public static JsonObjectMapper getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new JsonObjectMapper();
        }

        return INSTANCE;
    }

    public ObjectMapper getObjectMapper() {
        return objectMapper;
    }

}
