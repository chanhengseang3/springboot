package com.construction.graphql.exception;

import graphql.ErrorType;
import graphql.GraphQLError;
import graphql.language.SourceLocation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GQLExceptionHandler extends RuntimeException implements GraphQLError {
    private String code;

    public GQLExceptionHandler(String code) {
        this.code = code;
    }

    @Override
    public List<SourceLocation> getLocations() {
        return null;
    }

    @Override
    public ErrorType getErrorType() {
        return null;
    }

    @Override
    public Map<String, Object> getExtensions() {
        Map<String, Object> errorCode = new HashMap<>();
        errorCode.put("code", code);
        return errorCode;
    }
}
