package io.github.ladium1.erp.salescustomer.internal.init;

import io.github.ladium1.erp.salescustomer.internal.entity.SalesAssignment;
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
import java.util.Locale;

/**
 * 활성 영업 담당 배정의 고객사·직원 유일성 제약을 기존 DB 에도 보장한다.
 * <p>
 * MariaDB 는 null 을 포함한 UNIQUE 조합을 여러 행에 허용하므로, 활성 행에만 employee_id 를 투영하는
 * 생성 컬럼을 사용한다. 종료 행의 투영값은 null 이 되어 이력을 여러 건 보관할 수 있다.
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "app.schema-maintenance.enabled", havingValue = "true", matchIfMissing = true)
@Order(10)
@RequiredArgsConstructor
public class SalesAssignmentSchemaMigrator implements ApplicationRunner {

    static final String TABLE = "sales_assignments";
    static final String GENERATED_COLUMN = "active_employee_id";
    static final String UNIQUE_INDEX = SalesAssignment.ACTIVE_ASSIGNMENT_UNIQUE_CONSTRAINT;
    static final String ADD_GENERATED_COLUMN = "ALTER TABLE " + TABLE
            + " ADD COLUMN " + GENERATED_COLUMN + " BIGINT"
            + " GENERATED ALWAYS AS (CASE WHEN end_date IS NULL THEN employee_id ELSE NULL END)";
    static final String ADD_UNIQUE_INDEX = "ALTER TABLE " + TABLE
            + " ADD UNIQUE INDEX " + UNIQUE_INDEX + " (customer_id, " + GENERATED_COLUMN + ")";

    private final EntityManager em;

    @Override
    @Transactional
    public void run(@NonNull ApplicationArguments args) {
        List<?> expressions = generatedColumnExpressions();
        if (expressions.isEmpty()) {
            em.createNativeQuery(ADD_GENERATED_COLUMN).executeUpdate();
            log.info("sales_assignments 활성 담당자 생성 컬럼 추가");
        } else if (!isExpectedExpression(expressions.getFirst())) {
            throw new IllegalStateException("sales_assignments.active_employee_id 생성식이 예상한 활성 배정 규칙과 다릅니다.");
        }

        if (uniqueIndexExists()) {
            return;
        }

        List<?> duplicates = em.createNativeQuery("""
                SELECT customer_id, employee_id, COUNT(*)
                FROM sales_assignments
                WHERE end_date IS NULL
                GROUP BY customer_id, employee_id
                HAVING COUNT(*) > 1
                LIMIT 1
                """).getResultList();
        if (!duplicates.isEmpty()) {
            Object[] duplicate = (Object[]) duplicates.getFirst();
            throw new IllegalStateException(
                    "활성 영업 담당자 중복을 먼저 정리해야 합니다: customerId=" + duplicate[0]
                            + ", employeeId=" + duplicate[1] + ", count=" + duplicate[2]);
        }

        em.createNativeQuery(ADD_UNIQUE_INDEX).executeUpdate();
        log.info("sales_assignments 활성 고객사·직원 유일 인덱스 추가");
    }

    private List<?> generatedColumnExpressions() {
        return em.createNativeQuery("""
                SELECT GENERATION_EXPRESSION
                FROM information_schema.COLUMNS
                WHERE TABLE_SCHEMA = DATABASE()
                  AND TABLE_NAME = :table
                  AND COLUMN_NAME = :column
                """)
                .setParameter("table", TABLE)
                .setParameter("column", GENERATED_COLUMN)
                .getResultList();
    }

    private boolean uniqueIndexExists() {
        List<?> columns = em.createNativeQuery("""
                SELECT COLUMN_NAME, NON_UNIQUE
                FROM information_schema.STATISTICS
                WHERE TABLE_SCHEMA = DATABASE()
                  AND TABLE_NAME = :table
                  AND INDEX_NAME = :index
                ORDER BY SEQ_IN_INDEX
                """)
                .setParameter("table", TABLE)
                .setParameter("index", UNIQUE_INDEX)
                .getResultList();
        if (columns.isEmpty()) {
            return false;
        }
        if (columns.size() != 2
                || !indexColumnMatches(columns.get(0), "customer_id")
                || !indexColumnMatches(columns.get(1), GENERATED_COLUMN)) {
            throw new IllegalStateException("sales_assignments 활성 담당자 유일 인덱스 구성이 예상과 다릅니다.");
        }
        return true;
    }

    private boolean isExpectedExpression(Object expression) {
        if (expression == null) {
            return false;
        }
        String normalized = expression.toString()
                .replace("`", "")
                .replaceAll("\\s+", " ")
                .toLowerCase(Locale.ROOT)
                .trim();
        return normalized.contains("end_date is null")
                && normalized.contains("then employee_id")
                && normalized.contains("else null");
    }

    private boolean indexColumnMatches(Object row, String expectedColumn) {
        Object[] values = (Object[]) row;
        return expectedColumn.equalsIgnoreCase(values[0].toString())
                && ((Number) values[1]).intValue() == 0;
    }
}
