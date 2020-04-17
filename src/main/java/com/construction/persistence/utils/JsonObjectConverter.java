package com.construction.persistence.utils;

import com.construction.appconfiguration.ApplicationConfiguration;
import com.fasterxml.jackson.core.JsonProcessingException;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.io.IOException;
import java.util.Map;

@Converter
@SuppressWarnings("unchecked")
public class JsonObjectConverter implements AttributeConverter<Map<String, Object>, String> {

    @Override
    public String convertToDatabaseColumn(final Map<String, Object> value) {
        try {
            return value == null ?
                    null :
                    ApplicationConfiguration.OBJECT_MAPPER.writeValueAsString(value);
        } catch (final JsonProcessingException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public  Map<String, Object>  convertToEntityAttribute(final String value) {
        try {
            return value == null ?
                    null :
                    ApplicationConfiguration.OBJECT_MAPPER.readValue(value, Map.class);
        } catch (final IOException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

}
