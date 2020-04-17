package com.construction.graphql;

import com.construction.graphql.scalars.DurationScalar;
import com.introproventures.graphql.jpa.query.autoconfigure.GraphQLJpaQueryProperties;
import com.introproventures.graphql.jpa.query.autoconfigure.GraphQLSchemaConfigurer;
import com.introproventures.graphql.jpa.query.autoconfigure.GraphQLShemaRegistration;
import com.introproventures.graphql.jpa.query.schema.JavaScalars;
import com.introproventures.graphql.jpa.query.schema.impl.GraphQLJpaSchemaBuilder;
import com.construction.graphql.generator.schema.GQLMutationSchemaBuilder;
import graphql.schema.GraphQLSchema;
import org.springframework.context.annotation.Configuration;

import javax.persistence.EntityManager;
import java.time.Duration;

@Configuration
public class SchemaConfig implements GraphQLSchemaConfigurer {

    private final GraphQLJpaQueryProperties properties;
    private final GQLMutationSchemaBuilder mutationSchemaBuilder;
    private final EntityManager entityManager;

    public SchemaConfig(final GraphQLJpaQueryProperties properties,
                        final GQLMutationSchemaBuilder mutationSchemaBuilder,
                        final EntityManager entityManager) {
        this.properties = properties;
        this.mutationSchemaBuilder = mutationSchemaBuilder;
        this.entityManager = entityManager;
    }

    public GraphQLSchema querySchema() {
        JavaScalars.register(Duration.class, new DurationScalar());
        return new GraphQLJpaSchemaBuilder(entityManager)
                .name("CoreSchema")
                .useDistinctParameter(properties.isUseDistinctParameter())
                .defaultDistinct(properties.isDefaultDistinct())
                .enableRelay(properties.isEnableRelay())
                .toManyDefaultOptional(true)
                .build();
    }

    @Override
    public void configure(GraphQLShemaRegistration registry) {
        registry.register(querySchema());
        registry.register(mutationSchemaBuilder.build());
    }
}
