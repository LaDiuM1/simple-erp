package io.github.ladium1.erp.global.audit.internal.repository;

import io.github.ladium1.erp.global.audit.internal.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
}
