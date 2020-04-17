package com.construction.graphql.instrumentation;

import graphql.execution.AbortExecutionException;
import graphql.execution.instrumentation.InstrumentationContext;
import graphql.execution.instrumentation.SimpleInstrumentation;
import graphql.execution.instrumentation.parameters.InstrumentationValidationParameters;
import graphql.validation.ValidationError;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

import static java.lang.String.format;

@Component
public class MaxQuerySizeInstrumentation extends SimpleInstrumentation {

    @Value("${fs.graphql.query.size-limit}")
    private int defaultSizeLimit;

    @Override
    public InstrumentationContext<List<ValidationError>> beginValidation(final InstrumentationValidationParameters parameters) {
        if (parameters.getQuery().length() > defaultSizeLimit) {
            throw new AbortExecutionException(format("query size limit of %s exceeded", defaultSizeLimit));
        }
        return super.beginValidation(parameters);
    }

}
