package io.github.ladium1.erp.department.internal.web;

import io.github.ladium1.erp.department.internal.service.DepartmentService;
import io.github.ladium1.erp.global.security.MenuPermissionEvaluator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.reset;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DepartmentController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(DepartmentControllerTest.MethodSecurityTestConfig.class)
@WithMockUser
class DepartmentControllerTest {

    @TestConfiguration
    @EnableMethodSecurity
    static class MethodSecurityTestConfig {
    }

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private DepartmentService departmentService;

    @MockitoBean(name = "menuPermissionEvaluator")
    private MenuPermissionEvaluator menuPermissionEvaluator;

    @BeforeEach
    void allow_all_permissions() {
        given(menuPermissionEvaluator.canRead(any(), any())).willReturn(true);
        given(menuPermissionEvaluator.canWrite(any(), any())).willReturn(true);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "APPROVALS", "EXPENSES", "ATTENDANCE", "SALES_CUSTOMERS", "CONTRACTS", "AFTER_SERVICES"
    })
    @WithMockUser
    void employee_reference_consumers_can_read_departments(String menuCode) throws Exception {
        reset(menuPermissionEvaluator);
        given(menuPermissionEvaluator.canRead(any(), any())).willAnswer(invocation ->
                menuCode.equals(invocation.getArgument(1)));
        given(departmentService.findAll()).willReturn(List.of());

        mockMvc.perform(get("/api/v1/departments"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/departments/summary"))
                .andExpect(status().isForbidden());
    }
}
