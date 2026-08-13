package io.github.ladium1.erp.global.security.internal;

import io.github.ladium1.erp.employee.internal.dto.EmployeeProfileResponse;
import io.github.ladium1.erp.employee.internal.service.EmployeeService;
import io.github.ladium1.erp.employee.internal.web.EmployeeController;
import io.github.ladium1.erp.global.security.MenuPermissionEvaluator;
import io.github.ladium1.erp.global.demo.DemoRequestGuardFilter;
import io.github.ladium1.erp.global.demo.DemoIngressGuardFilter;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(EmployeeController.class)
@Import({SecurityConfig.class, JwtTokenProvider.class})
@TestPropertySource(properties = {
        "jwt.secret=bXktc3VwZXItc2VjcmV0LXRlc3Qta2V5LWZvci1qd3QtcHJvdmlkZXItMzJieXRlcw==",
        "jwt.expiration=60",
        "cors.allowed-origins=http://localhost:5173"
})
class JwtAccountStatusIntegrationTest {

    private static final String LOGIN_ID = "status-user";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private EmployeeService employeeService;

    @MockitoBean
    private MenuPermissionEvaluator menuPermissionEvaluator;

    @MockitoBean
    private DemoRequestGuardFilter demoRequestGuardFilter;

    @MockitoBean
    private DemoIngressGuardFilter demoIngressGuardFilter;

    @BeforeEach
    void continueAfterDemoGuard() throws Exception {
        doAnswer(invocation -> {
            FilterChain chain = invocation.getArgument(2);
            chain.doFilter(invocation.getArgument(0), invocation.getArgument(1));
            return null;
        }).when(demoRequestGuardFilter).doFilter(any(), any(), any());
        doAnswer(invocation -> {
            FilterChain chain = invocation.getArgument(2);
            chain.doFilter(invocation.getArgument(0), invocation.getArgument(1));
            return null;
        }).when(demoIngressGuardFilter).doFilter(any(), any(), any());
    }

    @Test
    @DisplayName("토큰 발급 뒤 퇴사 처리되면 같은 토큰을 401로 거부")
    void rejects_same_token_after_account_becomes_resigned() throws Exception {
        String token = jwtTokenProvider.createToken(LOGIN_ID, "USER");
        EmployeeProfileResponse profile = EmployeeProfileResponse.builder()
                .loginId(LOGIN_ID)
                .name("상태 전환 사용자")
                .menuPermissions(List.of())
                .build();
        given(employeeService.isLoginAllowed(LOGIN_ID)).willReturn(true, false);
        given(employeeService.getMyInfo(LOGIN_ID)).willReturn(profile);

        mockMvc.perform(get("/api/v1/employees/me")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/employees/me")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isUnauthorized());

        verify(employeeService, times(2)).isLoginAllowed(LOGIN_ID);
        verify(employeeService).getMyInfo(LOGIN_ID);
    }

    @Test
    @DisplayName("휴직처럼 로그인 가능한 상태는 기존 토큰 사용 허용")
    void allows_token_for_login_enabled_account() throws Exception {
        String token = jwtTokenProvider.createToken(LOGIN_ID, "USER");
        given(employeeService.isLoginAllowed(LOGIN_ID)).willReturn(true);
        given(employeeService.getMyInfo(LOGIN_ID)).willReturn(EmployeeProfileResponse.builder()
                .loginId(LOGIN_ID)
                .name("휴직 사용자")
                .menuPermissions(List.of())
                .build());

        mockMvc.perform(get("/api/v1/employees/me")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("JWT 자체 검증에 실패하면 계정 조회 없이 401로 거부")
    void rejects_invalid_token_without_account_lookup() throws Exception {
        mockMvc.perform(get("/api/v1/employees/me")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer invalid.token"))
                .andExpect(status().isUnauthorized());

        verify(employeeService, never()).isLoginAllowed(anyString());
    }
}
