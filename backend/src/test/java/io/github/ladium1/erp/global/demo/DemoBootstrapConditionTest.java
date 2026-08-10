package io.github.ladium1.erp.global.demo;

import io.github.ladium1.erp.coderule.internal.init.CodeRuleInitializer;
import io.github.ladium1.erp.coderule.internal.init.CodeRuleSchemaMigrator;
import io.github.ladium1.erp.coderule.internal.repository.CodeRuleRepository;
import io.github.ladium1.erp.employee.internal.init.EmployeeInitializer;
import io.github.ladium1.erp.employee.internal.repository.EmployeeRepository;
import io.github.ladium1.erp.global.jpa.internal.EnumColumnVarcharMigrator;
import io.github.ladium1.erp.global.storage.internal.service.FileOwnershipBackfill;
import io.github.ladium1.erp.global.storage.internal.service.FileStorageCleanupJob;
import io.github.ladium1.erp.global.storage.internal.service.FileStorageService;
import io.github.ladium1.erp.product.internal.init.ProductCategoryInitializer;
import io.github.ladium1.erp.product.internal.repository.ProductCategoryRepository;
import io.github.ladium1.erp.role.api.RoleApi;
import io.github.ladium1.erp.salescustomer.internal.init.SalesAssignmentSchemaMigrator;
import io.github.ladium1.erp.supplier.internal.init.SupplierInitializer;
import io.github.ladium1.erp.supplier.internal.repository.SupplierRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.boot.convert.ApplicationConversionService;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Clock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class DemoBootstrapConditionTest {

    @Test
    @DisplayName("schema maintenance는 기본 활성이고 명시적으로 끌 수 있음")
    void schema_maintenance_has_safe_switch() {
        ApplicationContextRunner runner = new ApplicationContextRunner()
                .withBean(EntityManager.class, () -> mock(EntityManager.class))
                .withUserConfiguration(
                        EnumColumnVarcharMigrator.class,
                        CodeRuleSchemaMigrator.class,
                        SalesAssignmentSchemaMigrator.class,
                        FileOwnershipBackfill.class
                );

        runner.run(context -> {
            assertThat(context).hasSingleBean(EnumColumnVarcharMigrator.class);
            assertThat(context).hasSingleBean(CodeRuleSchemaMigrator.class);
            assertThat(context).hasSingleBean(SalesAssignmentSchemaMigrator.class);
            assertThat(context).hasSingleBean(FileOwnershipBackfill.class);
        });
        runner.withPropertyValues("app.schema-maintenance.enabled=false").run(context -> {
            assertThat(context).doesNotHaveBean(EnumColumnVarcharMigrator.class);
            assertThat(context).doesNotHaveBean(CodeRuleSchemaMigrator.class);
            assertThat(context).doesNotHaveBean(SalesAssignmentSchemaMigrator.class);
            assertThat(context).doesNotHaveBean(FileOwnershipBackfill.class);
        });
    }

    @Test
    @DisplayName("reference bootstrap는 기본 활성이고 데모에서 독립적으로 끌 수 있음")
    void reference_bootstrap_has_independent_switch() {
        ApplicationContextRunner runner = new ApplicationContextRunner()
                .withBean(CodeRuleRepository.class, () -> mock(CodeRuleRepository.class))
                .withBean(ProductCategoryRepository.class, () -> mock(ProductCategoryRepository.class))
                .withBean(SupplierRepository.class, () -> mock(SupplierRepository.class))
                .withUserConfiguration(
                        CodeRuleInitializer.class,
                        ProductCategoryInitializer.class,
                        SupplierInitializer.class
                );

        runner.run(context -> {
            assertThat(context).hasSingleBean(CodeRuleInitializer.class);
            assertThat(context).hasSingleBean(ProductCategoryInitializer.class);
            assertThat(context).hasSingleBean(SupplierInitializer.class);
        });
        runner.withPropertyValues("app.reference-bootstrap.enabled=false").run(context -> {
            assertThat(context).doesNotHaveBean(CodeRuleInitializer.class);
            assertThat(context).doesNotHaveBean(ProductCategoryInitializer.class);
            assertThat(context).doesNotHaveBean(SupplierInitializer.class);
        });
    }

    @Test
    @DisplayName("복구 운영 관리자는 reference bootstrap와 분리된 switch를 사용")
    void admin_bootstrap_has_independent_switch() {
        ApplicationContextRunner runner = new ApplicationContextRunner()
                .withPropertyValues(
                        "app.admin-login-id=private.ops",
                        "app.admin-password=private-password",
                        "app.reference-bootstrap.enabled=false"
                )
                .withBean(EmployeeRepository.class, () -> mock(EmployeeRepository.class))
                .withBean(RoleApi.class, () -> mock(RoleApi.class))
                .withBean(PasswordEncoder.class, () -> mock(PasswordEncoder.class))
                .withUserConfiguration(EmployeeInitializer.class);

        runner.run(context -> assertThat(context).hasSingleBean(EmployeeInitializer.class));
        runner.withPropertyValues("app.admin-bootstrap.enabled=false")
                .run(context -> assertThat(context).doesNotHaveBean(EmployeeInitializer.class));
    }

    @Test
    @DisplayName("파일 정리 작업은 기본 활성이고 데모에서 끄면 등록되지 않음")
    void file_cleanup_has_demo_switch() {
        ApplicationContextRunner runner = new ApplicationContextRunner()
                .withInitializer(context -> context.getBeanFactory().setConversionService(
                        ApplicationConversionService.getSharedInstance()))
                .withBean(FileStorageService.class, () -> mock(FileStorageService.class))
                .withBean(Clock.class, Clock::systemUTC)
                .withUserConfiguration(FileStorageCleanupJob.class);

        runner.run(context -> assertThat(context).hasSingleBean(FileStorageCleanupJob.class));
        runner.withPropertyValues("erp.storage.cleanup.enabled=false")
                .run(context -> assertThat(context).doesNotHaveBean(FileStorageCleanupJob.class));
    }

    @Test
    @DisplayName("canonical seed verifier는 demo와 validation이 모두 켜진 경우에만 등록")
    void seed_verifier_requires_both_demo_flags() {
        ApplicationContextRunner runner = new ApplicationContextRunner()
                .withBean(JdbcTemplate.class, () -> mock(JdbcTemplate.class))
                .withBean(PasswordEncoder.class, () -> mock(PasswordEncoder.class))
                .withBean(DemoProperties.class, DemoProperties::new)
                .withBean(DemoStateStore.class, () -> mock(DemoStateStore.class))
                .withBean(DemoFileGenerationVerifier.class,
                        () -> mock(DemoFileGenerationVerifier.class))
                .withBean(DemoStartupVerificationGate.class,
                        () -> mock(DemoStartupVerificationGate.class))
                .withUserConfiguration(
                        DemoSeedPresenceVerifier.class,
                        DemoStartupReadyRunner.class
                );

        runner.withPropertyValues(
                        "demo.enabled=true",
                        "demo.seed.validation-enabled=true"
                )
                .run(context -> {
                    assertThat(context).hasSingleBean(DemoSeedPresenceVerifier.class);
                    assertThat(context).hasSingleBean(DemoStartupReadyRunner.class);
                });
        runner.withPropertyValues(
                        "demo.enabled=false",
                        "demo.seed.validation-enabled=true"
                )
                .run(context -> {
                    assertThat(context).doesNotHaveBean(DemoSeedPresenceVerifier.class);
                    assertThat(context).doesNotHaveBean(DemoStartupReadyRunner.class);
                });
        runner.withPropertyValues(
                        "demo.enabled=true",
                        "demo.seed.validation-enabled=false"
                )
                .run(context -> {
                    assertThat(context).doesNotHaveBean(DemoSeedPresenceVerifier.class);
                    assertThat(context).doesNotHaveBean(DemoStartupReadyRunner.class);
                });
    }

    @Test
    @DisplayName("쓰기 허용 runner는 복구 운영 계정 bootstrap보다 뒤에 실행")
    void startup_ready_runner_is_ordered_after_all_mutators() {
        int employeeOrder = EmployeeInitializer.class.getAnnotation(Order.class).value();
        int readyOrder = DemoStartupReadyRunner.class.getAnnotation(Order.class).value();

        assertThat(employeeOrder).isLessThan(readyOrder);
        assertThat(readyOrder).isEqualTo(Ordered.LOWEST_PRECEDENCE);
    }
}
