package com.construction.graphql;

import com.construction.graphql.instrumentation.AuthInstrumentation;
import com.construction.graphql.instrumentation.MaxQueryDepthInstrumentation;
import com.construction.graphql.instrumentation.MaxQuerySizeInstrumentation;
import com.introproventures.graphql.jpa.query.autoconfigure.GraphQLSchemaFactoryBean;
import com.introproventures.graphql.jpa.query.schema.GraphQLExecutor;
import graphql.ExecutionInput;
import graphql.ExecutionResult;
import graphql.GraphQL;
import graphql.execution.instrumentation.ChainedInstrumentation;
import graphql.schema.GraphQLSchema;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import javax.transaction.Transactional;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static javax.transaction.Transactional.TxType.SUPPORTS;

@Component
@Primary
public class GQLExecutor implements GraphQLExecutor {

    private final GraphQL graphQL;

    public GQLExecutor(final GraphQLSchemaFactoryBean schemaFactoryBean,
                       final MaxQueryDepthInstrumentation maxQueryDepthInstrumentation,
                       final MaxQuerySizeInstrumentation maxQuerySizeInstrumentation,
                       final AuthInstrumentation authInstrumentation) throws Exception {
        final var instrumentation = new ChainedInstrumentation(
                List.of(
                        maxQueryDepthInstrumentation,
                        maxQuerySizeInstrumentation,
                        authInstrumentation
                ));
        this.graphQL = GraphQL.newGraphQL(GraphQLSchema.newSchema(schemaFactoryBean.getObject()).build())
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
