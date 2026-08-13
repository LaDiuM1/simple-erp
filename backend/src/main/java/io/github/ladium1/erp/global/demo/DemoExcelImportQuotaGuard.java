package io.github.ladium1.erp.global.demo;

import io.github.ladium1.erp.global.exception.BusinessException;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 성공한 고객/영업명부 Excel import 행을 감사 로그에 계정과 실제 생성 ID로 기록한다.
 * manifest lock을 보유한 동일 business transaction 안에서 quota 확인과 로그 기록을 수행하므로
 * backend 재시작이나 동시 import로 account/generation 누적 한도를 우회할 수 없다.
 */
@Component
public class DemoExcelImportQuotaGuard {

    private static final String LOCK_MANIFEST_SQL = """
            SELECT reset_at
            FROM demo_seed_manifest
            WHERE id = 1
            FOR UPDATE
            """;
    private static final String IMPORT_ROWS_FOR_UPDATE_SQL = """
            SELECT actor_login_id
            FROM audit_logs
            WHERE created_at >= ?
              AND target_type IN ('DEMO_CUSTOMER_EXCEL_IMPORT', 'DEMO_SALES_CONTACT_EXCEL_IMPORT')
            ORDER BY id
            FOR UPDATE
            """;
    private static final String FIND_ACTOR_SQL = """
            SELECT id
            FROM employees
            WHERE login_id = ?
            """;
    private static final String INSERT_AUDIT_SQL = """
            INSERT INTO audit_logs(
                created_at, updated_at, action, actor_id, actor_login_id,
                ip_address, menu_code, target_id, target_type, trace_id
            ) VALUES (CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6), 'CREATE', ?, ?, NULL, ?, ?, ?, NULL)
            """;

    private final JdbcTemplate jdbcTemplate;
    private final DemoProperties properties;

    public DemoExcelImportQuotaGuard(JdbcTemplate jdbcTemplate, DemoProperties properties) {
        this.jdbcTemplate = jdbcTemplate;
        this.properties = properties;
    }

    public void assertRowsAllowedAndRecord(ImportKind kind, List<Long> targetIds) {
        if (!properties.isEnabled()) {
            return;
        }
        String loginId = authenticatedLoginId();
        if (kind == null || targetIds == null || targetIds.isEmpty()
                || targetIds.stream().anyMatch(id -> id == null || id <= 0)) {
            throw new BusinessException(DemoErrorCode.DEMO_EXCEL_ROW_QUOTA_EXCEEDED);
        }
        if (!TransactionSynchronizationManager.isActualTransactionActive()) {
            throw new BusinessException(DemoErrorCode.DEMO_STORAGE_UNAVAILABLE);
        }

        try {
            LocalDateTime resetAt = jdbcTemplate.queryForObject(LOCK_MANIFEST_SQL, LocalDateTime.class);
            Long actorId = jdbcTemplate.queryForObject(FIND_ACTOR_SQL, Long.class, loginId);
            if (resetAt == null || actorId == null) {
                throw new BusinessException(DemoErrorCode.DEMO_STORAGE_UNAVAILABLE);
            }

            long accountRows = 0;
            long generationRows = 0;
            for (String actorLoginId : jdbcTemplate.query(
                    IMPORT_ROWS_FOR_UPDATE_SQL,
                    (resultSet, rowNumber) -> resultSet.getString("actor_login_id"),
                    resetAt
            )) {
                if (actorLoginId == null || actorLoginId.isBlank()) {
                    throw new BusinessException(DemoErrorCode.DEMO_STORAGE_UNAVAILABLE);
                }
                generationRows = Math.addExact(generationRows, 1);
                if (loginId.equals(actorLoginId)) {
                    accountRows = Math.addExact(accountRows, 1);
                }
            }

            DemoProperties.Upload limits = properties.getUpload();
            int incomingRows = targetIds.size();
            if (wouldExceed(accountRows, incomingRows, limits.getExcelAccountQuotaRows())
                    || wouldExceed(generationRows, incomingRows, limits.getExcelGenerationQuotaRows())) {
                throw new BusinessException(DemoErrorCode.DEMO_EXCEL_ROW_QUOTA_EXCEEDED);
            }

            for (Long targetId : targetIds) {
                if (jdbcTemplate.update(
                        INSERT_AUDIT_SQL,
                        actorId,
                        loginId,
                        kind.menuCode,
                        targetId,
                        kind.targetType
                ) != 1) {
                    throw new BusinessException(DemoErrorCode.DEMO_STORAGE_UNAVAILABLE);
                }
            }
        } catch (BusinessException guarded) {
            throw guarded;
        } catch (ArithmeticException | DataAccessException invalidQuotaState) {
            throw new BusinessException(DemoErrorCode.DEMO_STORAGE_UNAVAILABLE);
        }
    }

    private static boolean wouldExceed(long current, int incoming, int limit) {
        return current > limit || incoming > limit - current;
    }

    private static String authenticatedLoginId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || authentication.getName() == null || authentication.getName().isBlank()
                || authentication.getName().length() > 64) {
            throw new BusinessException(DemoErrorCode.DEMO_STORAGE_UNAVAILABLE);
        }
        return authentication.getName();
    }

    public enum ImportKind {
        CUSTOMER("CUSTOMERS", "DEMO_CUSTOMER_EXCEL_IMPORT"),
        SALES_CONTACT("SALES_CONTACTS", "DEMO_SALES_CONTACT_EXCEL_IMPORT");

        private final String menuCode;
        private final String targetType;

        ImportKind(String menuCode, String targetType) {
            this.menuCode = menuCode;
            this.targetType = targetType;
        }
    }
}
