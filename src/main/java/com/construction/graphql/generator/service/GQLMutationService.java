package com.construction.graphql.generator.service;

import graphql.GraphQLException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import java.lang.reflect.ParameterizedType;
import java.util.Collection;
import java.util.List;

import static java.lang.String.format;

@Service
@Slf4j
@SuppressWarnings("unchecked")
public class GQLMutationService {

    @PersistenceContext
    private EntityManager entityManager;

    @Transactional
    public <T> T save(final T entity) {
        entityManager.persist(entity);
        return entity;
    }

    @Transactional
    public <T> T update(final T entity) {
         entityManager.merge(entity);
         return entity;
    }

    @Transactional
    public <T> boolean delete(final Class<T> entityClass,
                              final Object entityId) {
        final var criteria = entityManager.getCriteriaBuilder().createCriteriaDelete(entityClass);
        final var root = criteria.from(entityClass);
        criteria.where(root.get("id").in(entityId));
        return entityManager.createQuery(criteria).executeUpdate() > 0;
    }

    public <T> List<T> getListResult(final Class<T> entityClass,
                                     final String fieldName,
                                     final Collection valueIds) {
        try {
            final var parameterizedType = (ParameterizedType) GQLReflectionCache.getField(entityClass, fieldName).getGenericType();
            final var genericType = (Class<T>) parameterizedType.getActualTypeArguments()[0];
            final var criteriaQuery = entityManager.getCriteriaBuilder().createQuery(genericType);
            final var root = criteriaQuery.from(genericType);
            criteriaQuery.where(root.get("id").in(valueIds));
            final var relateEntity = entityManager.createQuery(criteriaQuery).getResultList();
            if (relateEntity.size() == valueIds.size()) {
                return relateEntity;
            }
        } catch (Exception e) {
            log.error(format("%s with identifier %s does not exist", entityClass.getSimpleName(), valueIds), e);
        }
        throw new GraphQLException(format("%s with identifier %s does not exist", entityClass.getSimpleName(), valueIds));
    }

    public <T> T getSingleResult(final Class<T> entityClass,
                                 final Object entityId) {
        try {
            final var criteriaQuery = entityManager.getCriteriaBuilder().createQuery(entityClass);
            final var root = criteriaQuery.from(entityClass);
            criteriaQuery.where(root.get("id").in(entityId));
            final var typedQuery = entityManager.createQuery(criteriaQuery);
            if (typedQuery.getSingleResult() != null) {
                return typedQuery.getSingleResult();
            }
        } catch (Exception e) {
            log.error(format("%s with identifier %s does not exist", entityClass.getSimpleName(), entityId), e);
        }
        throw new GraphQLException(format("%s with identifier %s does not exist", entityClass.getSimpleName(), entityId));
    }

}
