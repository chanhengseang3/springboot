package com.construction.graphql.generator.service;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Stream;

import static java.lang.String.format;

public class GQLReflectionCache {

    private static final ConcurrentMap<String, Field> FIELD_CACHE = new ConcurrentHashMap<>();

    public static Field getField(final Class clazz,
                                 final String fieldName) {
        return FIELD_CACHE.computeIfAbsent(
                format("%s.%s", clazz.getCanonicalName(), fieldName),
                key -> resolveField(clazz, fieldName));

    }

    private static Field resolveField(final Class clazz,
                                      final String fieldName) {
        final var field = resolveFields(clazz)
                .filter(f -> fieldName.equals(f.getName()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException(format("Field %s not accessible", fieldName)));
        field.setAccessible(true);
        return field;
    }

    private static Stream<Field> resolveFields(final Class clazz) {
        return clazz.getSuperclass() == null ?
                Arrays.stream(clazz.getDeclaredFields()) :
                Stream.concat(Arrays.stream(clazz.getDeclaredFields()), resolveFields(clazz.getSuperclass()));
    }
}
