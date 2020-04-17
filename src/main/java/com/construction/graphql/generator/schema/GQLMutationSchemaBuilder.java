package com.construction.graphql.generator.schema;

import com.construction.graphql.annotation.GQLIgnoreGenerateType;
import com.construction.graphql.generator.GQLTypeResolver;
import com.construction.graphql.generator.datafetcher.GQLCreateDataFetcher;
import com.construction.graphql.generator.datafetcher.GQLDeleteDataFetcher;
import com.construction.graphql.generator.datafetcher.GQLUpdateDataFetcher;
import com.introproventures.graphql.jpa.query.schema.JavaScalars;
import com.construction.graphql.generator.GQLMutationConstants;
import graphql.schema.*;
import org.springframework.stereotype.Component;

import javax.persistence.EntityManager;
import javax.persistence.metamodel.EntityType;

@Component
public class GQLMutationSchemaBuilder {

    private final EntityManager entityManager;
    private final GQLDeleteDataFetcher deleteEntityDataFetcher;
    private final GQLUpdateDataFetcher updateEntityDataFetcher;
    private final GQLCreateDataFetcher createEntityDataFetcher;

    public GQLMutationSchemaBuilder(final EntityManager entityManager,
                                    final GQLDeleteDataFetcher deleteEntityDataFetcher,
                                    final GQLUpdateDataFetcher updateEntityDataFetcher,
                                    final GQLCreateDataFetcher createEntityDataFetcher) {
        this.entityManager = entityManager;
        this.deleteEntityDataFetcher = deleteEntityDataFetcher;
        this.updateEntityDataFetcher = updateEntityDataFetcher;
        this.createEntityDataFetcher = createEntityDataFetcher;
    }

    public GraphQLSchema build() {
        return GraphQLSchema.newSchema()
                .query(getQueryType())
                .mutation(getMutationType())
                .build();
    }

    private GraphQLObjectType getQueryType() {
        return GraphQLObjectType.newObject()
                .name(GQLMutationConstants.QUERY_NAME)
                .build();
    }

    private GraphQLObjectType getMutationType() {
        final var mutationType = GraphQLObjectType.newObject().name(GQLMutationConstants.MUTATION_NAME);
        entityManager.getMetamodel().getEntities().stream()
                .filter(GQLTypeResolver::notGraphQLIgnored)
                .forEach(entityType -> {
                    if(GQLTypeResolver.notIgnoreGenerate(entityType, GQLIgnoreGenerateType.CREATE)) {
                        mutationType.field(getCreateDefinition(entityType));
                    }
                    if(GQLTypeResolver.notIgnoreGenerate(entityType, GQLIgnoreGenerateType.UPDATE)) {
                        mutationType.field(getUpdateDefinition(entityType));
                    }
                    if(GQLTypeResolver.notIgnoreGenerate(entityType, GQLIgnoreGenerateType.DELETE)) {
                        mutationType.field(getDeleteDefinition(entityType));
                    }
                });
        return mutationType.build();
    }

    private GraphQLFieldDefinition getCreateDefinition(final EntityType<?> entityType) {
        final var inputType = GraphQLArgument.newArgument()
                .name(GQLMutationConstants.INPUT)
                .type(new GraphQLNonNull(GQLTypeResolver.getEntityCreateInputType(entityType)))
                .build();
        return GraphQLFieldDefinition.newFieldDefinition()
                .name(GQLMutationConstants.CREATE_PREFIX + entityType.getName())
                .type(GQLTypeResolver.geEntityOutputType(entityType))
                .dataFetcher(createEntityDataFetcher)
                .argument(inputType)
                .build();
    }

    private GraphQLFieldDefinition getDeleteDefinition(final EntityType<?> entityType) {
        final GraphQLObjectType resultType = GraphQLObjectType.newObject()
                .name(GQLMutationConstants.DELETE_PREFIX + entityType.getName())
                .field(resolveIdDefinition(entityType))
                .build();
        return GraphQLFieldDefinition.newFieldDefinition()
                .name(GQLMutationConstants.DELETE_PREFIX + entityType.getName())
                .type(resultType)
                .dataFetcher(deleteEntityDataFetcher)
                .argument(resolveIdArgument(entityType))
                .build();
    }

    private GraphQLFieldDefinition getUpdateDefinition(final EntityType<?> entityType) {
        final GraphQLArgument inputTypeFields = GraphQLArgument.newArgument()
                .name(GQLMutationConstants.INPUT)
                .type(new GraphQLNonNull(GQLTypeResolver.getEntityUpdateInputType(entityType)))
                .build();
        return GraphQLFieldDefinition.newFieldDefinition()
                .name(GQLMutationConstants.UPDATE_PREFIX + entityType.getName())
                .dataFetcher(updateEntityDataFetcher)
                .type(GQLTypeResolver.geEntityOutputType(entityType))
                .argument(resolveIdArgument(entityType))
                .argument(inputTypeFields)
                .build();
    }

    private GraphQLFieldDefinition resolveIdDefinition(final EntityType<?> entityType) {
        final var idType = entityType.getIdType().getJavaType();
        return  GraphQLFieldDefinition.newFieldDefinition()
                .name("id")
                .type(new GraphQLNonNull(JavaScalars.of(idType)))
                .build();
    }

    private GraphQLArgument resolveIdArgument(final EntityType<?> entityType) {
        final var idType = entityType.getIdType().getJavaType();
        return  GraphQLArgument.newArgument()
                .name("id")
                .type(new GraphQLNonNull(JavaScalars.of(idType)))
                .build();
    }

}
