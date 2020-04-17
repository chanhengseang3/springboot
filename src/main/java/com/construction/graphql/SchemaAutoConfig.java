package com.construction.graphql;

import com.introproventures.graphql.jpa.query.autoconfigure.GraphQLSchemaAutoConfiguration;
import com.introproventures.graphql.jpa.query.autoconfigure.GraphQLSchemaFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SchemaAutoConfig extends GraphQLSchemaAutoConfiguration {

    @Bean
    @Override
    public GraphQLSchemaFactoryBean graphQLSchemaFactoryBean() {
        return super.graphQLSchemaFactoryBean();
    }
}
