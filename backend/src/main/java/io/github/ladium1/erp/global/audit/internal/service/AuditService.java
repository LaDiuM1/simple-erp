package io.github.ladium1.erp.global.audit.internal.service;

import io.github.ladium1.erp.global.audit.AuditAction;
import io.github.ladium1.erp.global.audit.internal.entity.AuditLog;
import io.github.ladium1.erp.global.demo.DemoProtectionPolicy;
import io.github.ladium1.erp.global.menu.Menu;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuditService {

    private final AuditLogWriter auditLogWriter;
    private final DemoProtectionPolicy demoProtectionPolicy;

    // 도메인 commit 이후 별도 트랜잭션으로 기록하고, audit 실패는 업무 결과에 영향을 주지 않는다.
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
                    .ipAddress(demoProtectionPolicy.auditIp(ipAddress))
                    .build();
            auditLogWriter.write(audit);
        } catch (Exception e) {
            log.error("Audit log write failed: menu={} action={} target={}#{}",
                    menu, action, targetType, targetId, e);
        }
    }

    private static String blankToNull(String s) {
        return (s == null || s.isBlank()) ? null : s;
    }
}
