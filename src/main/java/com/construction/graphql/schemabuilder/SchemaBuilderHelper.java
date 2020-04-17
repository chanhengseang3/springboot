package com.construction.graphql.schemabuilder;

import graphql.schema.GraphQLSchema;
import graphql.schema.idl.RuntimeWiring;
import graphql.schema.idl.SchemaGenerator;
import graphql.schema.idl.SchemaParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public abstract class SchemaBuilderHelper {

    private final String path;

    public SchemaBuilderHelper(final String path) {
        this.path = path;
    }

    public GraphQLSchema buildSchema() {
        try (final var reader = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream(path)))) {
            return new SchemaGenerator().makeExecutableSchema(
                    new SchemaParser().parse(reader),
                    buildWiring());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    protected abstract RuntimeWiring buildWiring();

}
