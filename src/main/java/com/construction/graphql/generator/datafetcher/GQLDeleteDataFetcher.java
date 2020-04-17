package com.construction.graphql.generator.datafetcher;

import com.construction.graphql.generator.service.GQLMutationService;
import com.construction.persistence.service.EntityNameList;
import com.construction.basic.audit.domain.ActionType;
import com.construction.basic.audit.service.AuditLogService;
import graphql.GraphQLException;
import graphql.schema.DataFetchingEnvironment;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

import static java.lang.String.format;

@Component
@Slf4j
public class GQLDeleteDataFetcher extends GQLBaseDataFetcher {

    private final GQLMutationService mutationService;
    private final AuditLogService logService;

    public GQLDeleteDataFetcher(final GQLMutationService mutationService,
                                final EntityNameList entityNameList,
                                final AuditLogService logService) {
        super(mutationService, entityNameList);
        this.mutationService = mutationService;
        this.logService = logService;
    }

    @Override
    public Map get(final DataFetchingEnvironment environment) {
        final var entityId = environment.getArgument("id");
        final var entityType = getEntityType(environment);
        try {
            final var foundEntity = mutationService.getSingleResult(entityType.getJavaType(), entityId);
            if (foundEntity != null) {
                if (mutationService.delete(foundEntity.getClass(), entityId)) {
                    return map(entityId);
                }
            }
            logService.insertAuditLog(entityType.getName(), ActionType.DELETE, entityId.toString());
        } catch (Exception e) {
            log.error(format("Delete %s with identifier %s failed", entityType.getName(), entityId), e);
        }
        throw new GraphQLException(format("Delete %s with identifier %s failed", entityType.getName(), entityId));
    }
}
