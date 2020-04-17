package com.construction.graphql.v0;

import com.introproventures.graphql.jpa.query.autoconfigure.GraphQLJpaQueryProperties;
import com.introproventures.graphql.jpa.query.schema.JavaScalars;
import com.introproventures.graphql.jpa.query.schema.impl.GraphQLJpaSchemaBuilder;
import com.construction.graphql.scalars.DurationScalar;
import graphql.schema.GraphQLSchema;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.persistence.EntityManager;
import java.time.Duration;

@Configuration
@Qualifier("classicConfig")
public class ClassicSchemaConfig {

    private final GraphQLJpaQueryProperties properties;
    private final EntityManager entityManager;

    public ClassicSchemaConfig(final GraphQLJpaQueryProperties properties,
                               final EntityManager entityManager) {
        this.properties = properties;
        this.entityManager = entityManager;
    }

    @Bean(name = "classicSchema")
    public GraphQLSchema graphQLSchemaBuilder() {
        JavaScalars.register(Duration.class, new DurationScalar());
        return new GraphQLJpaSchemaBuilder(entityManager)
                .name("ClassicSchema")
                .useDistinctParameter(properties.isUseDistinctParameter())
                .defaultDistinct(properties.isDefaultDistinct())
                .enableRelay(false)
                .build();
    }

}
