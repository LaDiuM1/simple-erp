package io.github.ladium1.erp.salescustomer.internal.init;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class SalesAssignmentSchemaMigratorConditionTest {

    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withBean(EntityManager.class, () -> mock(EntityManager.class))
            .withUserConfiguration(SalesAssignmentSchemaMigrator.class);

    @Test
    @DisplayName("배정 스키마 보완은 기본 활성이고 명시적으로 끌 수 있음")
    void schema_maintenance_has_safe_switch() {
        runner.run(context -> assertThat(context).hasSingleBean(SalesAssignmentSchemaMigrator.class));
        runner.withPropertyValues("app.schema-maintenance.enabled=false")
                .run(context -> assertThat(context).doesNotHaveBean(SalesAssignmentSchemaMigrator.class));
    }
}
