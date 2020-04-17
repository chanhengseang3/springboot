package com.construction.appconfiguration;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.util.StdDateFormat;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ApplicationConfiguration {

    public static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
            .setDateFormat(new StdDateFormat())
            .registerModules(new JavaTimeModule())
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

    @Bean
    public ObjectMapper objectMapper() {
        return OBJECT_MAPPER;
    }
}
