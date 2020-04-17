package com.construction.graphql.generator;

import com.construction.graphql.annotation.GQLIgnoreGenerate;
import com.construction.graphql.annotation.GQLIgnoreGenerateType;
import com.introproventures.graphql.jpa.query.annotation.GraphQLIgnore;
import com.introproventures.graphql.jpa.query.schema.JavaScalars;
import graphql.Scalars;
import graphql.schema.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import javax.persistence.*;
import javax.persistence.metamodel.*;
import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Member;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static java.lang.String.format;

@Slf4j
@SuppressWarnings("unchecked")
public class GQLTypeResolver {

    private static final String SUFFIX_ENUM = "Enum";
    private static final Map<Class<?>, GraphQLOutputType> CLASS_CACHE = new ConcurrentHashMap<>();
    private static final Map<EntityType<?>, GraphQLObjectType> ENTITY_OUTPUT_CACHE = new ConcurrentHashMap<>();
    private static final Map<EntityType<?>, GraphQLInputObjectType> CREATE_INPUT_CACHE = new ConcurrentHashMap<>();
    private static final Map<EntityType<?>, GraphQLInputObjectType> UPDATE_INPUT_CACHE = new ConcurrentHashMap<>();
    private static final Map<Class<?>, GraphQLObjectType> EMBEDDABLE_OUTPUT_CACHE = new ConcurrentHashMap<>();
    private static final Map<Class<?>, GraphQLInputObjectType> EMBEDDABLE_INPUT_CACHE = new ConcurrentHashMap<>();

    public static GraphQLObjectType geEntityOutputType(EntityType<?> entityType) {
        return ENTITY_OUTPUT_CACHE.computeIfAbsent(entityType, GQLTypeResolver::computeEntityOutputType);
    }

    public static GraphQLInputObjectType getEntityCreateInputType(EntityType<?> entityType) {
        return CREATE_INPUT_CACHE.computeIfAbsent(entityType, GQLTypeResolver::computeEntityCreateInputType);
    }

    public static GraphQLInputObjectType getEntityUpdateInputType(EntityType<?> entityType) {
        return UPDATE_INPUT_CACHE.computeIfAbsent(entityType, GQLTypeResolver::computeEntityUpdateInputType);
    }

    public static boolean isEmbeddable(final Attribute attribute) {
        return attribute.getPersistentAttributeType() == Attribute.PersistentAttributeType.EMBEDDED;
    }

    public static boolean isBasic(final Attribute attribute) {
        return attribute.getPersistentAttributeType() == Attribute.PersistentAttributeType.BASIC;
    }

    public static boolean isElementCollection(final Attribute attribute) {
        return attribute.getPersistentAttributeType() == Attribute.PersistentAttributeType.ELEMENT_COLLECTION;
    }

    public static boolean isToMany(final Attribute attribute) {
        return attribute.getPersistentAttributeType() == Attribute.PersistentAttributeType.ONE_TO_MANY
                || attribute.getPersistentAttributeType() == Attribute.PersistentAttributeType.MANY_TO_MANY;
    }

    public static boolean isToOne(final Attribute<?, ?> attribute) {
        return attribute.getPersistentAttributeType() == Attribute.PersistentAttributeType.MANY_TO_ONE
                || attribute.getPersistentAttributeType() == Attribute.PersistentAttributeType.ONE_TO_ONE;
    }

    public static boolean notIgnoreGenerate(final Attribute attribute,
                                            final GQLIgnoreGenerateType operation) {
        return notIgnoreGenerate(attribute.getJavaMember(), operation);
    }

    public static boolean notIgnoreGenerate(final EntityType entityType,
                                            final GQLIgnoreGenerateType operation) {
        return notIgnoreGenerate(entityType.getJavaType(), operation);
    }

    private static boolean notIgnoreGenerate(final Member member,
                                             final GQLIgnoreGenerateType operation) {
        return member instanceof AnnotatedElement && notIgnoreGenerate((AnnotatedElement) member, operation);
    }

    private static boolean notIgnoreGenerate(final AnnotatedElement annotatedElement,
                                             final GQLIgnoreGenerateType operation) {
        if (annotatedElement != null
                && annotatedElement.getAnnotation(GQLIgnoreGenerate.class) != null) {
            final var operations = EnumSet.of(operation, GQLIgnoreGenerateType.ALL);
            return Arrays.stream(annotatedElement.getAnnotation(GQLIgnoreGenerate.class).ignores())
                    .noneMatch(operations::contains);
        }
        return true;
    }

    private static boolean insertableAttribute(final Attribute attribute) {
        final var column = getAnnotation(attribute.getJavaMember(), Column.class);
        return column == null || column.insertable();
    }

    private static boolean nullableAttribute(final Attribute attribute) {
        final var member = attribute.getJavaMember();
        // Prefer use of  @Nullable, @NotNull
        if (hasAnnotation(member, Nullable.class)) {
            return true;
        } else if (hasAnnotation(member, NonNull.class)) {
            return false;
        } else if (isElementCollection(attribute)) {
            return true;
        } else if (isEmbeddable(attribute)) {
            return nullableEmbeddedAttribute((SingularAttribute) attribute);
        } else if (isToOne(attribute)) {
            return isOptionalAttribute(attribute);
        }
        // Default to column definition
        else {
            final var column = getAnnotation(member, Column.class);
            return column == null || column.nullable();
        }
    }

    private static boolean isOptionalAttribute(final Attribute attribute) {
        final var manyToOne = getAnnotation(attribute.getJavaMember(), ManyToOne.class);
        if (manyToOne != null) {
            return manyToOne.optional();
        }
        final var oneToOne = getAnnotation(attribute.getJavaMember(), OneToOne.class);
        if (oneToOne != null) {
            return oneToOne.optional();
        }
        return true;
    }

    private static boolean nullableEmbeddedAttribute(final SingularAttribute attribute) {
        final EmbeddableType<?> embeddableType = (EmbeddableType) attribute.getType();
        return embeddableType.getAttributes()
                .stream()
                .filter(GQLTypeResolver::notGraphQLIgnored)
                .allMatch(GQLTypeResolver::nullableAttribute);
    }

    private static boolean updatableAttribute(final Class<?> entityClazz,
                                              final Attribute attribute) {
        try {
            final var field = entityClazz.getDeclaredField(attribute.getName());
            final var joinColumn = field.getAnnotation(JoinColumn.class);
            if (joinColumn != null && !joinColumn.updatable()) {
                return false;
            }
            final var column = field.getAnnotation(Column.class);
            if (column != null && !column.updatable()) {
                return false;
            }
        } catch (NoSuchFieldException ignored) {
            log.debug(format("Field %s.%s is not found for check updatable. ", entityClazz.getSimpleName(), attribute.getName()));
        }
        return true;
    }

    private static <T extends Annotation> boolean hasAnnotation(final Member member,
                                                                final Class<T> annotation) {
        return getAnnotation(member, annotation) != null;
    }

    private static <T extends Annotation> T getAnnotation(final Member member,
                                                          final Class<T> annotation) {
        return member instanceof AnnotatedElement ?
                ((AnnotatedElement) member).getAnnotation(annotation) :
                null;
    }

    private static boolean isVersionAttribute(final Attribute attribute) {
        final var member = attribute.getJavaMember();
        return member instanceof AnnotatedElement && hasAnnotation(member, Version.class);
    }

    private static GraphQLInputObjectField getInputObjectField(final Attribute attribute) {
        return GraphQLInputObjectField.newInputObjectField()
                .name(attribute.getName())
                .type(nullableAttribute(attribute) ?
                        getInputType(attribute) :
                        new GraphQLNonNull(getInputType(attribute)))
                .build();
    }

    private static GraphQLObjectType computeEntityOutputType(final EntityType<?> entityType) {
        return GraphQLObjectType.newObject()
                .name(entityType.getName() + GQLMutationConstants.OUTPUT_SUFFIX)
                .fields(entityType.getAttributes()
                        .stream()
                        .filter(GQLTypeResolver::notGraphQLIgnored)
                        .map(GQLTypeResolver::getOutputDefinition)
                        .collect(Collectors.toList()))
                .build();
    }

    private static GraphQLInputObjectType computeEntityUpdateInputType(final EntityType<?> entityType) {
        return GraphQLInputObjectType.newInputObject()
                .name(GQLMutationConstants.UPDATE_PREFIX + entityType.getName() + GQLMutationConstants.INPUT_SUFFIX)
                .fields(entityType.getAttributes().stream()
                        .filter(it -> GQLTypeResolver.notIgnoreGenerate(it, GQLIgnoreGenerateType.UPDATE))
                        .filter(GQLTypeResolver::notGraphQLIgnored)
                        .filter(GQLTypeResolver::isOptionalAttribute)
                        .filter(it -> GQLTypeResolver.updatableAttribute(entityType.getJavaType(), it))
                        .filter(it -> !GQLTypeResolver.isIdAttribute(it))
                        .filter(it -> !GQLTypeResolver.isVersionAttribute(it))
                        .map(GQLTypeResolver::getUpdateInputField)
                        .collect(Collectors.toList()))
                .build();
    }

    private static GraphQLInputObjectType computeEntityCreateInputType(final EntityType<?> entityType) {
        return GraphQLInputObjectType.newInputObject()
                .name(GQLMutationConstants.CREATE_PREFIX + entityType.getName() + GQLMutationConstants.INPUT_SUFFIX)
                .fields(entityType.getAttributes().stream()
                        .filter(GQLTypeResolver::notGraphQLIgnored)
                        .filter(GQLTypeResolver::insertableAttribute)
                        .filter(it -> !GQLTypeResolver.isIdAttribute(it))
                        .filter(it -> !GQLTypeResolver.isVersionAttribute(it))
                        .filter(it -> !GQLTypeResolver.isTransient(it))
                        .map(GQLTypeResolver::getCreateInputField)
                        .collect(Collectors.toList()))
                .build();
    }

    private static GraphQLInputType getInputType(final Attribute<?, ?> attribute) {
        try {
            return (GraphQLInputType) getType(attribute, true);
        } catch (ClassCastException e) {
            throw new IllegalArgumentException("Attribute " + attribute + " cannot be mapped as an Input Argument");
        }
    }

    private static GraphQLOutputType getOutputType(final Attribute attribute) {
        try {
            return (GraphQLOutputType) getType(attribute, false);
        } catch (ClassCastException e) {
            throw new IllegalArgumentException("Attribute " + attribute + " cannot be mapped as an Output Select");
        }
    }

    private static GraphQLInputObjectField getCreateInputField(final Attribute<?, ?> attribute) {
        return GraphQLInputObjectField.newInputObjectField()
                .name(resolveInputAttributeName(attribute))
                .type(nullableAttribute(attribute) ?
                        getInputType(attribute) :
                        new GraphQLNonNull(getInputType(attribute)))
                .build();
    }

    private static GraphQLInputObjectField getUpdateInputField(final Attribute<?, ?> attribute) {
        return GraphQLInputObjectField.newInputObjectField()
                .name(resolveInputAttributeName(attribute))
                .type(getInputType(attribute))
                .build();
    }

    private static String resolveInputAttributeName(final Attribute<?, ?> attribute) {
        if (isToOne(attribute)) {
            return attribute.getName() + GQLMutationConstants.SINGLE_ID;
        } else if (isToMany(attribute)) {
            return attribute.getName() + GQLMutationConstants.MULTIPLE_ID;
        }
        return attribute.getName();
    }

    private static GraphQLFieldDefinition getOutputDefinition(final Attribute<?, ?> attribute) {
        return GraphQLFieldDefinition.newFieldDefinition()
                .name(attribute.getName())
                .type(getOutputType(attribute))
                .dataFetcher(PropertyDataFetcher.fetching(attribute.getName()))
                .argument(new ArrayList<>())
                .build();
    }

    private static GraphQLType getType(final Attribute attribute,
                                       final boolean input) {
        if (isBasic(attribute)) {
            return getOutputType(attribute.getJavaType());
        } else if (isEmbeddable(attribute)) {
            return getEmbeddableType((SingularAttribute) attribute, input);
        } else if (isToMany(attribute)) {
            return getToManyType((PluralAttribute) attribute, input);
        } else if (isToOne(attribute)) {
            return getToOneType((SingularAttribute) attribute, input);
        } else if (isElementCollection(attribute)) {
            return getCollectionType((PluralAttribute) attribute, input);
        }
        throw new UnsupportedOperationException(
                format("Attribute could not be mapped to GraphQL: field '%s' of entity class '%s'",
                        attribute.getJavaMember().getName(),
                        attribute.getDeclaringType().getJavaType().getName()));
    }

    private static boolean isTransient(final Attribute attribute) {
        return getAnnotation(attribute.getJavaMember(), Transient.class) != null;
    }

    private static boolean notGraphQLIgnored(final Attribute attribute) {
        return notGraphQLIgnored(attribute.getJavaMember()) && notGraphQLIgnored(attribute.getJavaType());
    }

    private static boolean isIdAttribute(final Attribute attribute) {
        return attribute instanceof SingularAttribute && ((SingularAttribute) attribute).isId();
    }

    public static boolean notGraphQLIgnored(final EntityType entityType) {
        return notGraphQLIgnored(entityType.getJavaType());
    }

    private static boolean notGraphQLIgnored(final Member member) {
        return member instanceof AnnotatedElement && notGraphQLIgnored((AnnotatedElement) member);
    }

    private static boolean notGraphQLIgnored(final AnnotatedElement annotatedElement) {
        return annotatedElement != null && annotatedElement.getAnnotation(GraphQLIgnore.class) == null;
    }

    private static GraphQLType getEmbeddableType(final SingularAttribute attribute,
                                                 final boolean input) {
        return input ?
                getEmbeddableInputType((EmbeddableType) attribute.getType()) :
                getEmbeddableOutputType((EmbeddableType) attribute.getType());
    }

    private static GraphQLType getEmbeddableInputType(final EmbeddableType<?> embeddableType) {
        var type = EMBEDDABLE_INPUT_CACHE.get(embeddableType.getJavaType());
        if (type == null) { // (avoid computeIfAbsent due to recursive calls)
            final var embeddableTypeName = format("%s%sEmbeddableType%s",
                    GQLMutationConstants.CREATE_PREFIX,
                    embeddableType.getJavaType().getSimpleName(),
                    "Input");
            type = GraphQLInputObjectType.newInputObject()
                    .name(embeddableTypeName)
                    .fields(embeddableType.getAttributes().stream()
                            .filter(GQLTypeResolver::notGraphQLIgnored)
                            .map(GQLTypeResolver::getInputObjectField)
                            .collect(Collectors.toList())
                    )
                    .build();
            EMBEDDABLE_INPUT_CACHE.put(embeddableType.getJavaType(), type);
        }
        return type;
    }

    private static GraphQLType getEmbeddableOutputType(final EmbeddableType<?> embeddableType) {
        var type = EMBEDDABLE_OUTPUT_CACHE.get(embeddableType.getJavaType());
        if (type == null) { // (avoid computeIfAbsent due to recursive calls)
            final var embeddableTypeName = format("%sEmbeddableType%s",
                    embeddableType.getJavaType().getSimpleName(),
                    GQLMutationConstants.OUTPUT_SUFFIX);
            type = GraphQLObjectType.newObject()
                    .name(embeddableTypeName)
                    .fields(embeddableType.getAttributes().stream()
                            .filter(GQLTypeResolver::notGraphQLIgnored)
                            .map(GQLTypeResolver::getOutputDefinition)
                            .collect(Collectors.toList())
                    )
                    .build();
            EMBEDDABLE_OUTPUT_CACHE.put(embeddableType.getJavaType(), type);
        }
        return type;
    }

    private static GraphQLOutputType getOutputType(final Class clazz) {
        if (clazz.isEnum()) {
            return CLASS_CACHE.computeIfAbsent(clazz, key -> {
                final var builder = GraphQLEnumType.newEnum().name(format("%s%s", clazz.getSimpleName(), SUFFIX_ENUM));
                Arrays.stream(clazz.getEnumConstants())
                        .map(v -> (Enum) v)
                        .forEach(v -> builder.value(v.name(), v));
                return builder.build();
            });
        } else {
            return JavaScalars.of(clazz);
        }
    }

    private static GraphQLType getToOneType(final SingularAttribute attribute,
                                            final boolean input) {
        final var entityType = (EntityType) attribute.getType();
        if (input) {
            final var idType = entityType.getIdType().getJavaType();
            return getOutputType(idType);
        } else {
            return new GraphQLTypeReference(entityType.getName() + GQLMutationConstants.OUTPUT_SUFFIX);
        }
    }

    private static GraphQLType getToManyType(final PluralAttribute attribute,
                                             final boolean input) {
        final var entityType = (EntityType) attribute.getElementType();
        if (input) {
            final var idType = entityType.getIdType().getJavaType();
            return new GraphQLList(getOutputType(idType));
        } else {
            return new GraphQLList(new GraphQLTypeReference(entityType.getName() + GQLMutationConstants.OUTPUT_SUFFIX));
        }
    }

    private static GraphQLType getCollectionType(final PluralAttribute attribute,
                                                 final boolean input) {
        final var foreignType = attribute.getElementType();
        if (input) {
            if (foreignType.getPersistenceType() == Type.PersistenceType.BASIC) {
                return new GraphQLList(getOutputType(foreignType.getJavaType()));
            } else {
                return new GraphQLList(Scalars.GraphQLLong);
            }
        } else {
            return new GraphQLList(getOutputType(foreignType.getJavaType()));
        }
    }

}
