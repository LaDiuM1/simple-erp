package io.github.ladium1.erp.global.security;

import io.github.ladium1.erp.department.api.DepartmentApi;
import io.github.ladium1.erp.employee.internal.excel.EmployeeExcelExporter;
import io.github.ladium1.erp.employee.internal.mapper.EmployeeMapper;
import io.github.ladium1.erp.employee.internal.repository.EmployeeRepository;
import io.github.ladium1.erp.employee.internal.service.EmployeeDataScopePrincipalReader;
import io.github.ladium1.erp.employee.internal.service.EmployeeService;
import io.github.ladium1.erp.global.demo.DemoProtectionPolicy;
import io.github.ladium1.erp.global.security.internal.PasswordEncodingConfiguration;
import io.github.ladium1.erp.position.api.PositionApi;
import io.github.ladium1.erp.role.api.RoleApi;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class DataScopeBeanDependencyTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withUserConfiguration(DataScopeDependencies.class);

    @Test
    void creates_employee_and_data_scope_services_without_dependency_cycle() {
        contextRunner.run(context -> {
            assertThat(context).hasNotFailed();
            assertThat(context).hasSingleBean(EmployeeService.class);
            assertThat(context).hasSingleBean(DataScopePrincipalReader.class);
            assertThat(context).hasSingleBean(DataScopeResolver.class);
            assertThat(context).hasSingleBean(DataScopeContextProvider.class);
        });
    }

    @TestConfiguration(proxyBeanMethods = false)
    @Import({
            EmployeeService.class,
            EmployeeDataScopePrincipalReader.class,
            DataScopeResolver.class,
            DataScopeContextProvider.class,
            PasswordEncodingConfiguration.class
    })
    static class DataScopeDependencies {

        @Bean
        EmployeeRepository employeeRepository() {
            return mock(EmployeeRepository.class);
        }

        @Bean
        EmployeeMapper employeeMapper() {
            return mock(EmployeeMapper.class);
        }

        @Bean
        RoleApi roleApi() {
            return mock(RoleApi.class);
        }

        @Bean
        DepartmentApi departmentApi() {
            return mock(DepartmentApi.class);
        }

        @Bean
        PositionApi positionApi() {
            return mock(PositionApi.class);
        }

        @Bean
        EmployeeExcelExporter employeeExcelExporter() {
            return mock(EmployeeExcelExporter.class);
        }

        @Bean
        DemoProtectionPolicy demoProtectionPolicy() {
            return mock(DemoProtectionPolicy.class);
        }
    }
}
