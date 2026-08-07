package io.github.ladium1.erp.global.audit.internal.service;

import io.github.ladium1.erp.global.audit.internal.entity.AuditLog;
import io.github.ladium1.erp.global.audit.internal.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class AuditLogWriter {

    private final AuditLogRepository auditLogRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void write(AuditLog auditLog) {
        auditLogRepository.saveAndFlush(auditLog);
    }
}
