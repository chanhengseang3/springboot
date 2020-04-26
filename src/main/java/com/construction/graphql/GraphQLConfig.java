package com.construction.graphql;

import com.construction.graphql.instrumentation.AuthInstrumentation;
import com.construction.graphql.instrumentation.MaxQueryDepthInstrumentation;
import com.construction.graphql.instrumentation.MaxQuerySizeInstrumentation;
import graphql.execution.instrumentation.ChainedInstrumentation;
import graphql.execution.instrumentation.Instrumentation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.annotation.RequestScope;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.function.Supplier;

@Configuration
public class GraphQLConfig {

    @Autowired
    private AuthInstrumentation authInstrumentation;
    @Autowired
    private MaxQuerySizeInstrumentation sizeInstrumentation;
    @Autowired
    private MaxQueryDepthInstrumentation depthInstrumentation;

    @Bean
    @RequestScope
    public Supplier<Instrumentation> instrumentation(HttpServletRequest request) {
        return () -> new ChainedInstrumentation(List.of(sizeInstrumentation, depthInstrumentation, authInstrumentation));
    }
}
