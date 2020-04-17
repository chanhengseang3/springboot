package com.construction.graphql.instrumentation;

import com.construction.appconfiguration.ApplicationSecurityContext;
import com.construction.user.authentication.domain.UserRole;
import com.construction.persistence.filter.FilterConfigurer;
import graphql.ExecutionResult;
import graphql.execution.AbortExecutionException;
import graphql.execution.instrumentation.InstrumentationContext;
import graphql.execution.instrumentation.SimpleInstrumentation;
import graphql.execution.instrumentation.parameters.InstrumentationExecuteOperationParameters;
import graphql.language.Field;
import graphql.language.OperationDefinition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static graphql.language.OperationDefinition.Operation.MUTATION;

@Component
public class AuthInstrumentation extends SimpleInstrumentation {

    @Autowired
    private ApplicationSecurityContext context;

    @Autowired
    private FilterConfigurer configurer;

    @Override
    public InstrumentationContext<ExecutionResult> beginExecuteOperation(final InstrumentationExecuteOperationParameters parameters) {
        final var executionContext = parameters.getExecutionContext();
        final var operationDefinition = executionContext.getOperationDefinition();
        if (MUTATION.equals(operationDefinition.getOperation())) {
            final var user = context.authenticatedUser();
            if (user == null) {
                throw new AbortExecutionException("Authorization required");
            } else if (user.getRole() != UserRole.ADMIN && !isSchemaRequest(operationDefinition)) {
                configurer.enableFilters(user.getUserName());
            }
        }
        return super.beginExecuteOperation(parameters);
    }

    private boolean isSchemaRequest(final OperationDefinition operationDefinition) {
        final var selection = operationDefinition.getSelectionSet().getSelections().get(0);
        return OperationDefinition.Operation.QUERY.equals(operationDefinition.getOperation())
                && selection instanceof Field
                && ((Field) selection).getName().startsWith("__schema");
    }

}
