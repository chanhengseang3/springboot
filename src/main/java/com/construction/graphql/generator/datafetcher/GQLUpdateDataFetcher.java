package com.construction.graphql.generator.datafetcher;

import com.construction.persistence.service.EntityNameList;
import com.construction.basic.audit.domain.ActionType;
import com.construction.basic.audit.service.AuditLogService;
import com.construction.graphql.generator.service.GQLMutationService;
import graphql.GraphQLException;
import graphql.schema.DataFetchingEnvironment;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Objects;

import static java.lang.String.format;

@Component
@Slf4j
public class GQLUpdateDataFetcher extends GQLBaseDataFetcher {

    private final GQLMutationService mutationService;
    private final AuditLogService logService;

    public GQLUpdateDataFetcher(final GQLMutationService mutationService,
                                final EntityNameList entityNameList,
                                final AuditLogService logService) {
        super(mutationService, entityNameList);
        this.mutationService = mutationService;
        this.logService = logService;
    }

    @Override
    public Map get(final DataFetchingEnvironment environment) throws GraphQLException {
        final var input = resolveInput(environment);
        final var entityType = getEntityType(environment);
        final var entityId = environment.getArgument("id");
        try {
            final var beanWrapper = new BeanWrapperImpl(mutationService.getSingleResult(entityType.getJavaType(), entityId));
            input.keySet().stream()
                    .map(entityType::getAttribute)
                    .filter(Objects::nonNull)
                    .forEach(attribute -> beanWrapper.setPropertyValue(
                            attribute.getName(),
                            resolveValue(entityType, input, attribute)));
            final var entity = mutationService.update(beanWrapper.getWrappedInstance());
            logService.insertAuditLog(entityType.getName(), ActionType.UPDATE, input.toString());
            return map(entity, environment.getField().getSelectionSet().getSelections());
        } catch (Exception e) {
            log.error(format("Update to %s with identifier %s failed", entityType.getName(), entityId), e);
            throw new GraphQLException(e.getMessage());
        }
    }

}
