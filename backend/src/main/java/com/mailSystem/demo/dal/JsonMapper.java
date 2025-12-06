package com.mailSystem.demo.dal;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

public class JsonMapper {
    private static ObjectMapper instance;

    private JsonMapper() {
    }

    public static synchronized ObjectMapper getInstance() {
        if (instance == null) {
            instance = new ObjectMapper();
            instance.registerModule(new JavaTimeModule()); // Handle Dates
            instance.enable(SerializationFeature.INDENT_OUTPUT); // Pretty print JSON
        }
        return instance;
    }
}