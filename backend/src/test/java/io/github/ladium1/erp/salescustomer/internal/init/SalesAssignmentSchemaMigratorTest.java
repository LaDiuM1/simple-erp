package io.github.ladium1.erp.salescustomer.internal.init;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.ApplicationArguments;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class SalesAssignmentSchemaMigratorTest {

    @InjectMocks
    private SalesAssignmentSchemaMigrator migrator;

    @Mock private EntityManager entityManager;
    @Mock private ApplicationArguments arguments;
    @Mock private Query columnQuery;
    @Mock private Query indexQuery;
    @Mock private Query duplicateQuery;
    @Mock private Query addColumnQuery;
    @Mock private Query addIndexQuery;

    @Test
    @DisplayName("기존 테이블에 생성 컬럼과 유일 인덱스 추가")
    void adds_generated_column_and_unique_index() {
        // given
        given(entityManager.createNativeQuery(contains("information_schema.COLUMNS"))).willReturn(columnQuery);
        stubParameters(columnQuery);
        given(columnQuery.getResultList()).willReturn(List.of());
        given(entityManager.createNativeQuery(SalesAssignmentSchemaMigrator.ADD_GENERATED_COLUMN))
                .willReturn(addColumnQuery);
        given(entityManager.createNativeQuery(contains("information_schema.STATISTICS"))).willReturn(indexQuery);
        stubParameters(indexQuery);
        given(indexQuery.getResultList()).willReturn(List.of());
        given(entityManager.createNativeQuery(contains("HAVING COUNT(*) > 1"))).willReturn(duplicateQuery);
        given(duplicateQuery.getResultList()).willReturn(List.of());
        given(entityManager.createNativeQuery(SalesAssignmentSchemaMigrator.ADD_UNIQUE_INDEX))
                .willReturn(addIndexQuery);

        // when
        migrator.run(arguments);

        // then
        verify(addColumnQuery).executeUpdate();
        verify(addIndexQuery).executeUpdate();
    }

    @Test
    @DisplayName("생성 컬럼과 유일 인덱스가 있으면 변경 없음")
    void skips_existing_schema() {
        // given
        given(entityManager.createNativeQuery(contains("information_schema.COLUMNS"))).willReturn(columnQuery);
        stubParameters(columnQuery);
        given(columnQuery.getResultList()).willReturn(List.of("case when end_date is null then employee_id else null end"));
        given(entityManager.createNativeQuery(contains("information_schema.STATISTICS"))).willReturn(indexQuery);
        stubParameters(indexQuery);
        given(indexQuery.getResultList()).willReturn(List.<Object[]>of(
                new Object[]{"customer_id", 0},
                new Object[]{"active_employee_id", 0}
        ));

        // when
        migrator.run(arguments);

        // then
        verify(entityManager, never()).createNativeQuery(SalesAssignmentSchemaMigrator.ADD_GENERATED_COLUMN);
        verify(entityManager, never()).createNativeQuery(SalesAssignmentSchemaMigrator.ADD_UNIQUE_INDEX);
    }

    @Test
    @DisplayName("활성 배정 중복이 있으면 유일 인덱스 추가 중단")
    void rejects_duplicate_active_assignments() {
        // given
        given(entityManager.createNativeQuery(contains("information_schema.COLUMNS"))).willReturn(columnQuery);
        stubParameters(columnQuery);
        given(columnQuery.getResultList()).willReturn(List.of("case when end_date is null then employee_id else null end"));
        given(entityManager.createNativeQuery(contains("information_schema.STATISTICS"))).willReturn(indexQuery);
        stubParameters(indexQuery);
        given(indexQuery.getResultList()).willReturn(List.of());
        given(entityManager.createNativeQuery(contains("HAVING COUNT(*) > 1"))).willReturn(duplicateQuery);
        given(duplicateQuery.getResultList()).willReturn(List.<Object[]>of(new Object[]{1L, 10L, 2L}));

        // when & then
        assertThatThrownBy(() -> migrator.run(arguments))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("customerId=1")
                .hasMessageContaining("employeeId=10")
                .hasMessageContaining("count=2");
        verify(entityManager, never()).createNativeQuery(SalesAssignmentSchemaMigrator.ADD_UNIQUE_INDEX);
    }

    @Test
    @DisplayName("같은 이름의 일반 컬럼이 있으면 마이그레이션 중단")
    void rejects_non_generated_column() {
        // given
        given(entityManager.createNativeQuery(contains("information_schema.COLUMNS"))).willReturn(columnQuery);
        stubParameters(columnQuery);
        given(columnQuery.getResultList()).willReturn(Arrays.asList((Object) null));

        // when & then
        assertThatThrownBy(() -> migrator.run(arguments))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("생성식이 예상한 활성 배정 규칙과 다릅니다");
        verify(entityManager, never()).createNativeQuery(SalesAssignmentSchemaMigrator.ADD_UNIQUE_INDEX);
    }

    private void stubParameters(Query query) {
        given(query.setParameter(eq("table"), eq(SalesAssignmentSchemaMigrator.TABLE))).willReturn(query);
        if (query == columnQuery) {
            given(query.setParameter(eq("column"), eq(SalesAssignmentSchemaMigrator.GENERATED_COLUMN)))
                    .willReturn(query);
        } else {
            given(query.setParameter(eq("index"), eq(SalesAssignmentSchemaMigrator.UNIQUE_INDEX)))
                    .willReturn(query);
        }
    }
}
