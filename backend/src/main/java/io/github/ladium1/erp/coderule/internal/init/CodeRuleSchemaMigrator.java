package io.github.ladium1.erp.coderule.internal.init;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * code_rules 의 폐기된 컬럼 (prefix / default_seq_length / parent_scoped / reset_policy) 을 정리한다.
 * <p>
 * 기존 row 의 {@code prefix} 와 {@code default_seq_length} 값은 {@code pattern} 안에 inline 으로 흡수
 * (예: {@code {PREFIX}{SEQ}} + prefix=D + dsl=3 -> {@code D{SEQ:3}}) 한 뒤 컬럼을 drop.
 * <p>
 * idempotent — 컬럼이 이미 사라진 환경에서 재실행해도 안전.
 * <p>
 * {@link CodeRuleInitializer} (@Order 100) 보다 먼저 실행되어야 신규 schema 가 보장된 상태에서 seed 한다.
 */
@Slf4j
@Component
@Order(50)
@RequiredArgsConstructor
public class CodeRuleSchemaMigrator implements ApplicationRunner {

    private final EntityManager em;

    @Override
    @Transactional
    public void run(@NonNull ApplicationArguments args) {
        if (!columnExists("code_rules", "prefix")) {
            return; // 이미 마이그레이션 완료
        }
        log.info("code_rules 폐기 컬럼 마이그레이션 시작 — prefix/default_seq_length 를 pattern 에 inline 흡수");

        @SuppressWarnings("unchecked")
        List<Object[]> rows = em.createNativeQuery(
                "SELECT id, COALESCE(prefix, ''), pattern, default_seq_length FROM code_rules"
        ).getResultList();

        for (Object[] row : rows) {
            Long id = ((Number) row[0]).longValue();
            String prefix = (String) row[1];
            String pattern = (String) row[2];
            int dsl = ((Number) row[3]).intValue();

            String next = pattern;
            if (prefix != null && !prefix.isEmpty()) {
                next = next.replace("{PREFIX}", prefix);
            }
            // {SEQ} (no-arg) -> {SEQ:dsl}
            next = next.replaceAll("\\{SEQ}", "{SEQ:" + dsl + "}");
            if (!pattern.equals(next)) {
                em.createNativeQuery("UPDATE code_rules SET pattern = :p WHERE id = :id")
                        .setParameter("p", next)
                        .setParameter("id", id)
                        .executeUpdate();
                log.info("code_rules id={} pattern: {} -> {}", id, pattern, next);
            }
        }

        dropColumnIfExists("code_rules", "prefix");
        dropColumnIfExists("code_rules", "default_seq_length");
        dropColumnIfExists("code_rules", "parent_scoped");
        dropColumnIfExists("code_rules", "reset_policy");

        log.info("code_rules 폐기 컬럼 마이그레이션 완료");
    }

    private boolean columnExists(String table, String column) {
        Number count = (Number) em.createNativeQuery(
                "SELECT COUNT(*) FROM information_schema.COLUMNS "
                        + "WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = :t AND COLUMN_NAME = :c"
        ).setParameter("t", table).setParameter("c", column).getSingleResult();
        return count.intValue() > 0;
    }

    private void dropColumnIfExists(String table, String column) {
        if (columnExists(table, column)) {
            em.createNativeQuery("ALTER TABLE " + table + " DROP COLUMN " + column).executeUpdate();
            log.info("DROP COLUMN {}.{}", table, column);
        }
    }
}
