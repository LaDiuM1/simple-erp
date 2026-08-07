package io.github.ladium1.erp;

import io.github.ladium1.erp.approval.api.ApprovalApi;
import io.github.ladium1.erp.approval.api.ApprovalDocType;
import io.github.ladium1.erp.approval.api.dto.ApprovalSubmitCommand;
import io.github.ladium1.erp.coderule.internal.init.CodeRuleInitializer;
import io.github.ladium1.erp.coderule.internal.init.CodeRuleSchemaMigrator;
import io.github.ladium1.erp.employee.api.LoginAccountApi;
import io.github.ladium1.erp.employee.internal.init.EmployeeInitializer;
import io.github.ladium1.erp.global.jpa.internal.EnumColumnVarcharMigrator;
import io.github.ladium1.erp.global.security.internal.JwtTokenProvider;
import io.github.ladium1.erp.global.storage.internal.service.FileOwnershipBackfill;
import io.github.ladium1.erp.global.storage.internal.service.FileStorageCleanupJob;
import io.github.ladium1.erp.global.validation.RequestTextPolicy;
import io.github.ladium1.erp.product.internal.init.ProductCategoryInitializer;
import io.github.ladium1.erp.salescustomer.internal.init.SalesAssignmentSchemaMigrator;
import io.github.ladium1.erp.supplier.internal.init.SupplierInitializer;
import jakarta.persistence.EntityManagerFactory;
import jakarta.validation.ConstraintViolationException;
import org.hibernate.dialect.H2Dialect;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.orm.jpa.support.OpenEntityManagerInViewInterceptor;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:erp-application-context;MODE=MariaDB;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.show-sql=false",
        "spring.jpa.properties.hibernate.format_sql=false",
        "jwt.secret=bXktc3VwZXItc2VjcmV0LXRlc3Qta2V5LWZvci1qd3QtcHJvdmlkZXItMzJieXRlcw==",
        "jwt.expiration=60",
        "cors.allowed-origins=http://localhost:5173",
        "app.admin-login-id=admin",
        "app.admin-password=test-password"
})
class ErpApplicationConfigurationTest {

    @Autowired
    private ApplicationContext context;

    @Autowired
    private EntityManagerFactory entityManagerFactory;

    @Autowired
    private ApprovalApi approvalApi;

    @Autowired
    private Environment environment;

    @Autowired
    @Qualifier("requestMappingHandlerMapping")
    private RequestMappingHandlerMapping requestMappingHandlerMapping;

    @MockitoBean
    private EnumColumnVarcharMigrator enumColumnVarcharMigrator;

    @MockitoBean
    private FileOwnershipBackfill fileOwnershipBackfill;

    @MockitoBean
    private CodeRuleSchemaMigrator codeRuleSchemaMigrator;

    @MockitoBean
    private CodeRuleInitializer codeRuleInitializer;

    @MockitoBean
    private SupplierInitializer supplierInitializer;

    @MockitoBean
    private ProductCategoryInitializer productCategoryInitializer;

    @MockitoBean
    private EmployeeInitializer employeeInitializer;

    @MockitoBean
    private FileStorageCleanupJob fileStorageCleanupJob;

    @MockitoBean
    private SalesAssignmentSchemaMigrator salesAssignmentSchemaMigrator;

    @Test
    void loads_custom_jwt_security_without_boot_default_user() {
        assertThat(context.getBeansOfType(UserDetailsService.class)).isEmpty();
        assertThat(context.getBeansOfType(SecurityFilterChain.class)).hasSize(1);
        assertThat(context.getBeansOfType(JwtTokenProvider.class)).hasSize(1);
        assertThat(context.getBeansOfType(LoginAccountApi.class)).hasSize(1);
        assertThat(context.getBeansOfType(PasswordEncoder.class)).hasSize(1);
    }

    @Test
    void disables_open_entity_manager_in_view_and_detects_the_database_dialect() {
        assertThat(context.getEnvironment().getProperty("spring.jpa.open-in-view", Boolean.class)).isFalse();
        assertThat(context.getEnvironment().containsProperty("spring.jpa.database-platform")).isFalse();
        assertThat(context.getBeansOfType(OpenEntityManagerInViewInterceptor.class)).isEmpty();
        assertThat(entityManagerFactory.unwrap(SessionFactoryImplementor.class)
                .getJdbcServices()
                .getDialect()).isInstanceOf(H2Dialect.class);
    }

    @Test
    void validates_cross_module_approval_content_before_service_execution() {
        ApprovalSubmitCommand command = ApprovalSubmitCommand.builder()
                .docType(ApprovalDocType.GENERAL)
                .title("제목")
                .content("가".repeat(RequestTextPolicy.MAX_LONG_TEXT_LENGTH + 1))
                .drafterId(1L)
                .approverIds(java.util.List.of(2L))
                .build();

        assertThatThrownBy(() -> approvalApi.submit(command))
                .isInstanceOf(ConstraintViolationException.class);
    }

    @Test
    void http_metric_uri_tag_limit_covers_all_route_templates_with_bounded_headroom() {
        Set<String> routeTemplates = requestMappingHandlerMapping.getHandlerMethods().keySet().stream()
                .flatMap(mapping -> mapping.getPatternValues().stream())
                .collect(Collectors.toSet());
        int configuredLimit = environment.getRequiredProperty(
                "management.metrics.web.server.max-uri-tags", Integer.class);

        assertThat(routeTemplates.size()).isGreaterThan(100);
        assertThat(configuredLimit)
                .isGreaterThanOrEqualTo(routeTemplates.size() + 4)
                .isLessThanOrEqualTo(512);
    }
}
