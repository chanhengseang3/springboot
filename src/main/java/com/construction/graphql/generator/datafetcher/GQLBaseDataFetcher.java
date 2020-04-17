package com.construction.graphql.generator.datafetcher;

import com.construction.persistence.service.EntityNameList;
import com.construction.graphql.generator.GQLTypeResolver;
import com.construction.graphql.generator.service.GQLMutationService;
import com.construction.graphql.generator.service.GQLReflectionCache;
import com.construction.graphql.generator.GQLMutationConstants;
import graphql.language.Field;
import graphql.language.Selection;
import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import org.springframework.beans.BeanWrapperImpl;

import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.EntityType;
import java.util.*;

import static java.lang.String.format;

@SuppressWarnings("unchecked")
public abstract class GQLBaseDataFetcher implements DataFetcher<Map> {

    private final GQLMutationService mutationService;
    private final EntityNameList entityNameList;

    GQLBaseDataFetcher(final GQLMutationService mutationService,
                       final EntityNameList entityNameList) {
        this.mutationService = mutationService;
        this.entityNameList = entityNameList;
    }

    Map map(final Object id) {
        return Map.of("id", id);
    }

    Map map(final Object entity,
            final List<Selection> selections) {
        final var result = new LinkedHashMap<>();
        selections.stream()
                .filter(Field.class::isInstance)
                .map(selection -> (Field) selection)
                .map(Field::getName)
                .forEach(fieldName -> result.put(fieldName, mapField(entity, fieldName)));
        return result;
    }

    private Object mapField(final Object entity,
                            final String fieldName) {
        try {
            return GQLReflectionCache.getField(entity.getClass(), fieldName).get(entity);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(format("Field %s.%s is not accessible", entity.getClass().getSimpleName(), fieldName));
        }
    }

    protected Object resolveValue(final EntityType<?> entityType,
                                  final Map input,
                                  final Attribute<?, ?> attribute) {
        if (GQLTypeResolver.isBasic(attribute)) {
            return input.get(attribute.getName());
        } else if (GQLTypeResolver.isElementCollection(attribute)) {
            return input.get(attribute.getName());
        } else if (GQLTypeResolver.isEmbeddable(attribute)) {
            return resolveEmbeddedValue(input, attribute);
        } else if (GQLTypeResolver.isToOne(attribute)) {
            return mutationService.getSingleResult(attribute.getJavaType(), input.get(attribute.getName()));
        } else if (GQLTypeResolver.isToMany(attribute)) {
            return mutationService.getListResult(entityType.getJavaType(), attribute.getName(), (Collection) input.get(attribute.getName()));
        }
        throw new UnsupportedOperationException();
    }

    protected EntityType<?> getEntityType(final DataFetchingEnvironment environment) {
        final var entity = environment.getField().getName().substring(6);
        return entityNameList.getEntityType(entity);
    }

    protected HashMap<String, Object> resolveInput(final DataFetchingEnvironment environment) {
        final var newInput = new HashMap<String, Object>();
        final var input = (HashMap<String, Object>) environment.getArguments().get(GQLMutationConstants.INPUT);
        final var entityType = getEntityType(environment);
        input.forEach((key, value) -> {
            if (hasEntityAttribute(entityType, key)) {
                newInput.put(key, value);
            } else {
                if (key.endsWith(GQLMutationConstants.SINGLE_ID)) {
                    newInput.put(removeSuffix(key, GQLMutationConstants.SINGLE_ID), value);
                } else if (key.endsWith(GQLMutationConstants.MULTIPLE_ID)) {
                    newInput.put(removeSuffix(key, GQLMutationConstants.MULTIPLE_ID), value);
                }
            }
        });
        return newInput;
    }

    private Object resolveEmbeddedValue(final Map input,
                                        final Attribute<?, ?> attribute) {
        final var embeddedBean = new BeanWrapperImpl(attribute.getJavaType());
        final var embeddedInput = (Map<String, Object>) input.get(attribute.getName());
        embeddedInput.forEach(embeddedBean::setPropertyValue);
        return embeddedBean.getWrappedInstance();
    }

    private static String removeSuffix(final String fieldName,
                                       final String suffix) {
        if (fieldName != null && fieldName.endsWith(suffix)) {
            return fieldName.substring(0, fieldName.length() - suffix.length());
        }
        return fieldName;
    }

    private boolean hasEntityAttribute(final EntityType<?> entityType,
                                       final String attributeName) {
       return entityType.getAttributes()
               .stream()
               .anyMatch(it -> it.getName().equals(attributeName));
    }

}
