package com.construction.graphql.generator.datafetcher;

import com.construction.graphql.generator.service.GQLMutationService;
import com.construction.persistence.service.EntityNameList;
import com.construction.basic.audit.domain.ActionType;
import com.construction.basic.audit.service.AuditLogService;
import graphql.GraphQLException;
import graphql.schema.DataFetchingEnvironment;
import lombok.extern.java.Log;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.stereotype.Component;

import javax.persistence.EntityExistsException;
import javax.persistence.PersistenceException;
import java.util.Map;
import java.util.Objects;

@Component
@Log
public class GQLCreateDataFetcher extends GQLBaseDataFetcher {

    private final GQLMutationService mutationService;
    private final AuditLogService logService;

    public GQLCreateDataFetcher(final GQLMutationService mutationService,
                                final EntityNameList entityNameList,
                                final AuditLogService logService) {
        super(mutationService, entityNameList);
        this.mutationService = mutationService;
        this.logService = logService;
    }

    @Override
    public Map get(final DataFetchingEnvironment environment) {
        final var input = resolveInput(environment);
        final var entityType = getEntityType(environment);
        final var beanWrapper = new BeanWrapperImpl(entityType.getJavaType());
        try {
            input.keySet().stream()
                    .map(entityType::getAttribute)
                    .filter(Objects::nonNull)
                    .forEach(attribute -> beanWrapper.setPropertyValue(
                            attribute.getName(),
                            resolveValue(entityType, input, attribute)));
            final var entity = mutationService.save(beanWrapper.getWrappedInstance());
            logService.insertAuditLog(entityType.getName(), ActionType.CREATE, input.toString());
            return map(entity, environment.getField().getSelectionSet().getSelections());
        } catch (EntityExistsException | IllegalArgumentException e) {
            throw new GraphQLException(e.getMessage());
        } catch (PersistenceException e) {
            log.warning(e.getMessage());
            throw new GraphQLException("Invalid input field", e);
        }
    }

}
