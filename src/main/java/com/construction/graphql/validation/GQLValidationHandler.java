package com.construction.graphql.validation;

import graphql.ErrorType;
import graphql.GraphQLError;
import graphql.language.SourceLocation;

import java.util.List;
import java.util.Map;

public class GQLValidationHandler implements GraphQLError {
    private final Map<String, Object> errorAttribute;
    private final Object path;
    private final String message;

    public GQLValidationHandler(Map<String, Object> errorAttribute, Object path, String message) {
        this.errorAttribute = errorAttribute;
        this.path = path;
        this.message = message;
    }

    @Override
    public String getMessage() {
        return message;
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
    public List<Object> getPath() {
        return List.of(path);
    }

    @Override
    public Map<String, Object> getExtensions() {
        return errorAttribute;
    }
}
