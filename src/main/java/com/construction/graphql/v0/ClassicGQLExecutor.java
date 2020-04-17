package com.construction.graphql.v0;

import com.construction.graphql.instrumentation.AuthInstrumentation;
import com.construction.graphql.instrumentation.MaxQueryDepthInstrumentation;
import com.construction.graphql.instrumentation.MaxQuerySizeInstrumentation;
import com.introproventures.graphql.jpa.query.schema.GraphQLExecutor;
import graphql.ExecutionInput;
import graphql.ExecutionResult;
import graphql.GraphQL;
import graphql.execution.instrumentation.ChainedInstrumentation;
import graphql.schema.GraphQLSchema;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import javax.transaction.Transactional;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static javax.transaction.Transactional.TxType.SUPPORTS;

@Component("classicExecutor")
public class ClassicGQLExecutor implements GraphQLExecutor {

    private final GraphQL graphQL;

    public ClassicGQLExecutor(@Qualifier("classicSchema") final GraphQLSchema graphQLSchema,
                              final MaxQueryDepthInstrumentation maxQueryDepthInstrumentation,
                              final MaxQuerySizeInstrumentation maxQuerySizeInstrumentation,
                              final AuthInstrumentation authInstrumentation) {
        final var instrumentation = new ChainedInstrumentation(
                List.of(
                        maxQueryDepthInstrumentation,
                        maxQuerySizeInstrumentation,
                        authInstrumentation
                ));
        this.graphQL = GraphQL.newGraphQL(
                GraphQLSchema.newSchema(graphQLSchema).build())
                .instrumentation(instrumentation)
                .build();
    }

    @Transactional(SUPPORTS)
    public ExecutionResult execute(final String query) {
        return execute(query, Map.of());
    }

    @Transactional(SUPPORTS)
    public ExecutionResult execute(final String query, final Map<String, Object> arguments) {
        final Map<String, Object> variables = arguments == null ? Collections.emptyMap() : arguments;
        return graphQL.execute(
                ExecutionInput.newExecutionInput()
                        .query(query)
                        .variables(variables)
                        .build());
    }

}
