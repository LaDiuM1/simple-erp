package io.github.ladium1.erp.employee.internal.web;

import io.github.ladium1.erp.employee.internal.dto.EmployeeCreateRequest;
import io.github.ladium1.erp.employee.internal.dto.EmployeeDetailResponse;
import io.github.ladium1.erp.employee.internal.dto.EmployeeProfileResponse;
import io.github.ladium1.erp.employee.internal.dto.EmployeeSummaryResponse;
import io.github.ladium1.erp.employee.internal.dto.EmployeeUpdateRequest;
import io.github.ladium1.erp.employee.internal.entity.EmployeeStatus;
import io.github.ladium1.erp.employee.internal.exception.EmployeeErrorCode;
import io.github.ladium1.erp.employee.internal.service.EmployeeService;
import io.github.ladium1.erp.global.exception.BusinessException;
import io.github.ladium1.erp.global.security.MenuPermissionEvaluator;
import io.github.ladium1.erp.global.web.PageResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(EmployeeController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(EmployeeControllerTest.TestWebMvcConfig.class)
class EmployeeControllerTest {

    /**
     * @WebMvcTest 슬라이스는 SecurityConfig 를 import 하지 않아 {@link AuthenticationPrincipalArgumentResolver}
     * 가 등록되지 않는다. {@code @AuthenticationPrincipal User user} 가 default databinder 로 fallback 되며 실패하므로
     * 본 테스트에서만 resolver 를 직접 등록.
     */
    @TestConfiguration
    static class TestWebMvcConfig implements WebMvcConfigurer {
        @Override
        public void addArgumentResolvers(java.util.List<HandlerMethodArgumentResolver> resolvers) {
            resolvers.add(new AuthenticationPrincipalArgumentResolver());
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private EmployeeService employeeService;

    @MockitoBean
    private MenuPermissionEvaluator menuPermissionEvaluator;

    @BeforeEach
    void allow_all_permissions() {
        given(menuPermissionEvaluator.canRead(any(), any())).willReturn(true);
        given(menuPermissionEvaluator.canWrite(any(), any())).willReturn(true);
    }

    @Test
    @WithMockUser(username = "테스트아이디")
    @DisplayName("내 정보 조회 성공")
    void get_my_info_success() throws Exception {
        // given
        String testId = "테스트아이디";
        String testName = "테스트이름";
        String testDepartment = "테스트부서";
        String testPosition = "테스트직책";
        String testRoleName = "테스트권한명";
        String testRoleCode = "TEST_ROLE";

        EmployeeProfileResponse mockResponse = EmployeeProfileResponse.builder()
                .id(1L)
                .loginId(testId)
                .name(testName)
                .departmentName(testDepartment)
                .positionName(testPosition)
                .roleName(testRoleName)
                .roleCode(testRoleCode)
                .menuPermissions(List.of())
                .build();

        given(employeeService.getMyInfo(any())).willReturn(mockResponse);

        // when & then
        mockMvc.perform(get("/api/v1/employees/me")
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.loginId").value(testId))
                .andExpect(jsonPath("$.data.name").value(testName))
                .andExpect(jsonPath("$.data.departmentName").value(testDepartment))
                .andExpect(jsonPath("$.data.roleCode").value(testRoleCode));
    }

    @Test
    @DisplayName("직원 목록 조회 성공")
    void search_success() throws Exception {
        // given
        EmployeeSummaryResponse summary = EmployeeSummaryResponse.builder()
                .id(1L)
                .loginId("hong")
                .name("홍길동")
                .status(EmployeeStatus.ACTIVE)
                .build();
        PageResponse<EmployeeSummaryResponse> page = new PageResponse<>(
                List.of(summary), 0, 20, 1, 1, false
        );
        given(employeeService.search(any(), any())).willReturn(page);

        // when & then
        mockMvc.perform(get("/api/v1/employees"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].loginId").value("hong"))
                .andExpect(jsonPath("$.data.totalElements").value(1));
    }

    @Test
    @DisplayName("직원 상세 조회 성공")
    void get_detail_success() throws Exception {
        // given
        EmployeeDetailResponse detail = EmployeeDetailResponse.builder()
                .id(7L)
                .loginId("hong")
                .name("홍길동")
                .status(EmployeeStatus.ACTIVE)
                .roleId(1L)
                .roleName("일반")
                .build();
        given(employeeService.getDetail(7L)).willReturn(detail);

        // when & then
        mockMvc.perform(get("/api/v1/employees/{id}", 7L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(7))
                .andExpect(jsonPath("$.data.name").value("홍길동"));
    }

    @Test
    @DisplayName("존재하지 않는 직원 조회 시 404")
    void get_detail_fail_not_found() throws Exception {
        // given
        given(employeeService.getDetail(99L))
                .willThrow(new BusinessException(EmployeeErrorCode.EMPLOYEE_NOT_FOUND));

        // when & then
        mockMvc.perform(get("/api/v1/employees/{id}", 99L))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("직원 등록 성공")
    void create_success() throws Exception {
        // given
        EmployeeCreateRequest request = new EmployeeCreateRequest(
                "newone", "secret123", "신참",
                "newone@example.com", "010-1111-2222",
                null, null, null,
                LocalDate.of(2026, 4, 1),
                EmployeeStatus.ACTIVE,
                1L, null, null
        );
        given(employeeService.create(any())).willReturn(42L);

        // when & then
        mockMvc.perform(post("/api/v1/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(42));
    }

    @Test
    @DisplayName("중복 로그인 ID 등록 시 409")
    void create_fail_duplicate_login_id() throws Exception {
        // given
        EmployeeCreateRequest request = new EmployeeCreateRequest(
                "exist", "secret123", "중복",
                null, null, null, null, null,
                LocalDate.of(2026, 4, 1),
                EmployeeStatus.ACTIVE,
                1L, null, null
        );
        willThrow(new BusinessException(EmployeeErrorCode.DUPLICATE_LOGIN_ID))
                .given(employeeService).create(any());

        // when & then
        mockMvc.perform(post("/api/v1/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("직원 수정 성공")
    void update_success() throws Exception {
        // given
        EmployeeUpdateRequest request = new EmployeeUpdateRequest(
                "이름변경",
                null, null, null, null, null,
                LocalDate.of(2026, 4, 1),
                EmployeeStatus.ACTIVE,
                1L, null, null,
                null
        );

        // when & then
        mockMvc.perform(put("/api/v1/employees/{id}", 7L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNoContent());
        verify(employeeService).update(eq(7L), any());
    }

    @Test
    @DisplayName("존재하지 않는 직원 수정 시 404")
    void update_fail_not_found() throws Exception {
        // given
        EmployeeUpdateRequest request = new EmployeeUpdateRequest(
                "이름",
                null, null, null, null, null,
                null, EmployeeStatus.ACTIVE,
                1L, null, null,
                null
        );
        willThrow(new BusinessException(EmployeeErrorCode.EMPLOYEE_NOT_FOUND))
                .given(employeeService).update(eq(99L), any());

        // when & then
        mockMvc.perform(put("/api/v1/employees/{id}", 99L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("직원 삭제 성공")
    void delete_success() throws Exception {
        // when & then
        mockMvc.perform(delete("/api/v1/employees/{id}", 7L))
                .andExpect(status().isNoContent());
        verify(employeeService).delete(7L);
    }

    @Test
    @DisplayName("존재하지 않는 직원 삭제 시 404")
    void delete_fail_not_found() throws Exception {
        // given
        willThrow(new BusinessException(EmployeeErrorCode.EMPLOYEE_NOT_FOUND))
                .given(employeeService).delete(99L);

        // when & then
        mockMvc.perform(delete("/api/v1/employees/{id}", 99L))
                .andExpect(status().isNotFound());
    }
}
