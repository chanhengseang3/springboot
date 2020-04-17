package com.construction.basic.audit.service;

import com.construction.appconfiguration.ApplicationSecurityContext;
import com.construction.basic.audit.domain.ActionType;
import com.construction.basic.audit.repository.AuditLogRepository;
import com.construction.basic.audit.domain.AuditLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.LocalDateTime;

@Service
public class AuditLogService {

    @Autowired
    private AuditLogRepository repository;

    @Autowired
    private ApplicationSecurityContext context;

    @Async
    @Transactional
    public void insertAuditLog(final String entityType, final ActionType actionType, final String input) {
        final var log = new AuditLog()
                .setUserName(String.valueOf(context.getAuthPrinciple()))
                .setTime(LocalDateTime.now())
                .setEntityType(entityType)
                .setActionType(actionType)
                .setInput(input)
                .setAction(actionType + " " + entityType + " with input: " + input);
        repository.save(log);
    }
}
