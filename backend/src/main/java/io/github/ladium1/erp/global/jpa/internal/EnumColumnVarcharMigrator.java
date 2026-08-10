package io.github.ladium1.erp.global.jpa.internal;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

/**
 * MariaDB 네이티브 {@code ENUM(...)} 컬럼을 {@code VARCHAR} 로 전환한다.
 * <p>
 * Hibernate 는 {@code @Enumerated(STRING)} 필드를 MariaDB 에서 고정 집합 네이티브 ENUM 컬럼으로 생성한다.
 * 그런데 {@code ddl-auto=update} 는 기존 ENUM 의 허용값 목록을 넓히지 않으므로, enum 에 상수를 추가하면
 * (예: {@code CodeRuleTarget} 에 새 도메인, {@code Menu} 에 새 메뉴) 기존 DB 의 해당 컬럼이 새 값을 거부하고
 * INSERT 가 {@code Data truncated for column} 으로 실패한다 — 매 부팅 도는 seed / reconcile 이 그대로 죽는다.
 * <p>
 * enum 컬럼을 VARCHAR 로 바꾸면 저장 표현 (enum name 문자열) 은 동일하고 값 집합 제약만 사라져,
 * 이후 enum 확장이 스키마 변경 없이 그대로 동작한다. Hibernate 가 새 테이블에 다시 네이티브 ENUM 을 만들어도
 * 스키마 생성 -> ApplicationRunner 실행 순서라 같은 부팅 사이클 안에서 이 러너가 VARCHAR 로 되돌린다.
 * <p>
 * idempotent — 이미 VARCHAR 인 컬럼은 건너뛴다 ({@code DATA_TYPE='enum'} 만 대상). NOT NULL / 컬럼 코멘트는 보존한다.
 * 프레임워크 소유 테이블 ({@code event_publication} — Spring Modulith) 은 제외한다.
 * 모든 seed / reconcile 러너보다 먼저 실행되어야 하므로 {@code @Order} 최소값을 갖는다.
 */
@Slf4j
@Component
@Order(0)
@ConditionalOnProperty(name = "app.schema-maintenance.enabled", havingValue = "true", matchIfMissing = true)
@RequiredArgsConstructor
public class EnumColumnVarcharMigrator implements ApplicationRunner {

    /** 우리 엔티티가 소유하지 않는 프레임워크 관리 테이블 — 스키마를 건드리지 않는다. */
    private static final Set<String> SKIP_TABLES = Set.of("event_publication");

    private static final int VARCHAR_LENGTH = 255;

    private final EntityManager em;

    @Override
    @Transactional
    public void run(@NonNull ApplicationArguments args) {
        @SuppressWarnings("unchecked")
        List<Object[]> enumColumns = em.createNativeQuery(
                "SELECT TABLE_NAME, COLUMN_NAME, IS_NULLABLE, COLUMN_COMMENT "
                        + "FROM information_schema.COLUMNS "
                        + "WHERE TABLE_SCHEMA = DATABASE() AND DATA_TYPE = 'enum'"
        ).getResultList();

        for (Object[] col : enumColumns) {
            String table = (String) col[0];
            String column = (String) col[1];
            boolean nullable = "YES".equals(col[2]);
            String comment = (String) col[3];

            if (SKIP_TABLES.contains(table)) {
                continue;
            }
            if (!isSafeIdentifier(table) || !isSafeIdentifier(column)) {
                log.warn("enum -> varchar 전환 건너뜀 — 예상 밖 식별자: {}.{}", table, column);
                continue;
            }

            StringBuilder ddl = new StringBuilder("ALTER TABLE `").append(table)
                    .append("` MODIFY `").append(column)
                    .append("` VARCHAR(").append(VARCHAR_LENGTH).append(")");
            if (!nullable) {
                ddl.append(" NOT NULL");
            }
            if (comment != null && !comment.isEmpty()) {
                ddl.append(" COMMENT '").append(comment.replace("'", "''")).append("'");
            }

            em.createNativeQuery(ddl.toString()).executeUpdate();
            log.info("enum -> varchar 전환: {}.{}", table, column);
        }
    }

    private boolean isSafeIdentifier(String identifier) {
        return identifier.matches("[A-Za-z0-9_]+");
    }
}
