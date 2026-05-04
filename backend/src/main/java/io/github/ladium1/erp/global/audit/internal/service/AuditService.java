package io.github.ladium1.erp.global.audit.internal.service;

import io.github.ladium1.erp.global.audit.AuditAction;
import io.github.ladium1.erp.global.audit.internal.entity.AuditLog;
import io.github.ladium1.erp.global.audit.internal.repository.AuditLogRepository;
import io.github.ladium1.erp.global.menu.Menu;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    // audit 실패가 도메인 트랜잭션을 막지 않도록 별도 트랜잭션 + swallow
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void record(Menu menu, AuditAction action,
                       String actorLoginId, Long actorId,
                       String targetType, Long targetId,
                       String traceId, String ipAddress) {
        try {
            AuditLog audit = AuditLog.builder()
                    .menuCode(menu)
                    .action(action)
                    .actorLoginId(actorLoginId)
                    .actorId(actorId)
                    .targetType(blankToNull(targetType))
                    .targetId(targetId)
                    .traceId(traceId)
                    .ipAddress(ipAddress)
                    .build();
            auditLogRepository.save(audit);
        } catch (Exception e) {
            log.error("Audit log INSERT failed: menu={} action={} target={}#{}",
                    menu, action, targetType, targetId, e);
        }
    }

    private static String blankToNull(String s) {
        return (s == null || s.isBlank()) ? null : s;
    }
}
