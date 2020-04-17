package com.construction.graphql.instrumentation;

import graphql.ExecutionResult;
import graphql.execution.AbortExecutionException;
import graphql.execution.instrumentation.InstrumentationContext;
import graphql.execution.instrumentation.SimpleInstrumentation;
import graphql.execution.instrumentation.parameters.InstrumentationExecuteOperationParameters;
import graphql.language.Field;
import graphql.language.Node;
import graphql.language.Selection;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import static java.lang.String.format;

@Component
public class MaxQueryDepthInstrumentation extends SimpleInstrumentation {

    @Value("${fs.graphql.query.depth-limit}")
    private int defaultMaxDepth;

    @Override
    public InstrumentationContext<ExecutionResult> beginExecuteOperation(final InstrumentationExecuteOperationParameters parameters) {
        for (final var selection : parameters.getExecutionContext().getOperationDefinition().getSelectionSet().getSelections()) {
            validateDepth(parameters, selection);
        }
        return super.beginExecuteOperation(parameters);
    }

    private void validateDepth(final InstrumentationExecuteOperationParameters parameters,
                               final Selection selection) {
        if (selection instanceof Field) {
            final var field = (Field) selection;
            if (!field.getName().equals("__schema")) {
                final var depth = resolveDepth(parameters.getExecutionContext().getDocument(), 0);
                if (depth > defaultMaxDepth) {
                    throw new AbortExecutionException(format("query depth of %d exceeds maximum query depth of %d", depth, defaultMaxDepth));
                }
            }
        }
    }

    private int resolveDepth(final Node<?> node,
                             final int currentDepth) {
        return node.getChildren()
                .stream()
                .map(child -> resolveDepth(child, child instanceof Field ? currentDepth + 1 : currentDepth))
                .max(Integer::compareTo)
                .orElse(currentDepth);
    }

}
