package io.github.ladium1.erp.attendance.internal.web;

import io.github.ladium1.erp.attendance.internal.dto.LeaveAdminResponse;
import io.github.ladium1.erp.attendance.internal.dto.LeaveBalanceAdminResponse;
import io.github.ladium1.erp.attendance.internal.dto.LeaveBalanceResponse;
import io.github.ladium1.erp.attendance.internal.dto.LeaveBalanceUpdateRequest;
import io.github.ladium1.erp.attendance.internal.dto.LeaveCreateRequest;
import io.github.ladium1.erp.attendance.internal.dto.LeaveResponse;
import io.github.ladium1.erp.attendance.internal.entity.LeaveStatus;
import io.github.ladium1.erp.attendance.internal.entity.LeaveType;
import io.github.ladium1.erp.attendance.internal.exception.AttendanceErrorCode;
import io.github.ladium1.erp.attendance.internal.service.LeaveService;
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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(LeaveController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(LeaveControllerTest.TestWebMvcConfig.class)
class LeaveControllerTest {

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

    private static final String LOGIN_ID = "testUser";

    // 2026-07-06 = 월요일
    private static final LocalDate MONDAY = LocalDate.of(2026, 7, 6);

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private LeaveService leaveService;

    @MockitoBean
    private MenuPermissionEvaluator menuPermissionEvaluator;

    @BeforeEach
    void allow_all_permissions() {
        given(menuPermissionEvaluator.canRead(any(), any())).willReturn(true);
        given(menuPermissionEvaluator.canWrite(any(), any())).willReturn(true);
    }

    @Test
    @WithMockUser(username = LOGIN_ID)
    @DisplayName("휴가 신청 성공")
    void create_success() throws Exception {
        // given
        LeaveCreateRequest request = new LeaveCreateRequest(
                LeaveType.ANNUAL, MONDAY, MONDAY.plusDays(4), "여름 휴가", List.of(2L)
        );
        given(leaveService.create(any(), any())).willReturn(42L);

        // when & then
        mockMvc.perform(post("/api/v1/leaves")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(42));
        verify(leaveService).create(eq(LOGIN_ID), any());
    }

    @Test
    @WithMockUser(username = LOGIN_ID)
    @DisplayName("휴가 유형 없는 신청 시 400")
    void create_fail_missing_leave_type() throws Exception {
        // given
        LeaveCreateRequest request = new LeaveCreateRequest(
                null, MONDAY, MONDAY, "유형 누락", List.of(2L)
        );

        // when & then
        mockMvc.perform(post("/api/v1/leaves")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
        verify(leaveService, never()).create(any(), any());
    }

    @Test
    @WithMockUser(username = LOGIN_ID)
    @DisplayName("잔여 부족 신청 시 409")
    void create_fail_insufficient_balance() throws Exception {
        // given
        LeaveCreateRequest request = new LeaveCreateRequest(
                LeaveType.ANNUAL, MONDAY, MONDAY.plusDays(4), "여름 휴가", List.of(2L)
        );
        given(leaveService.create(any(), any()))
                .willThrow(new BusinessException(AttendanceErrorCode.INSUFFICIENT_LEAVE_BALANCE));

        // when & then
        mockMvc.perform(post("/api/v1/leaves")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    @WithMockUser(username = LOGIN_ID)
    @DisplayName("내 휴가 목록 조회 성공")
    void get_my_leaves_success() throws Exception {
        // given
        LeaveResponse leave = new LeaveResponse(
                1L, LeaveType.ANNUAL, MONDAY, MONDAY.plusDays(4),
                new BigDecimal("5"), "여름 휴가", LeaveStatus.IN_PROGRESS,
                100L, null
        );
        given(leaveService.getMyLeaves(LOGIN_ID)).willReturn(List.of(leave));

        // when & then
        mockMvc.perform(get("/api/v1/leaves/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].leaveType").value("ANNUAL"))
                .andExpect(jsonPath("$.data[0].days").value(5))
                .andExpect(jsonPath("$.data[0].status").value("IN_PROGRESS"));
    }

    @Test
    @WithMockUser(username = LOGIN_ID)
    @DisplayName("내 잔여 연차 조회 성공")
    void get_my_balance_success() throws Exception {
        // given
        LeaveBalanceResponse balance = new LeaveBalanceResponse(
                2025, new BigDecimal("15"), new BigDecimal("3"), new BigDecimal("12")
        );
        given(leaveService.getMyBalance(LOGIN_ID, 2025)).willReturn(balance);

        // when & then
        mockMvc.perform(get("/api/v1/leaves/balance/me").param("year", "2025"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.grantedDays").value(15))
                .andExpect(jsonPath("$.data.remainingDays").value(12));
    }

    @Test
    @WithMockUser(username = LOGIN_ID)
    @DisplayName("연도 미지정 잔여 조회 — 올해로 위임")
    void get_my_balance_default_year() throws Exception {
        // given
        int currentYear = LocalDate.now().getYear();
        given(leaveService.getMyBalance(LOGIN_ID, currentYear))
                .willReturn(LeaveBalanceResponse.defaultOf(currentYear, new BigDecimal("15")));

        // when & then
        mockMvc.perform(get("/api/v1/leaves/balance/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.year").value(currentYear));
        verify(leaveService).getMyBalance(LOGIN_ID, currentYear);
    }

    @Test
    @WithMockUser(username = LOGIN_ID)
    @DisplayName("관리자 휴가 목록 조회 성공")
    void search_success() throws Exception {
        // given
        LeaveAdminResponse leave = new LeaveAdminResponse(
                1L, 2L, "홍길동", LeaveType.ANNUAL, MONDAY, MONDAY.plusDays(4),
                new BigDecimal("5"), "여름 휴가", LeaveStatus.APPROVED, 100L, null
        );
        PageResponse<LeaveAdminResponse> page = new PageResponse<>(List.of(leave), 0, 20, 1, 1, false);
        given(leaveService.search(any(), any())).willReturn(page);

        // when & then
        mockMvc.perform(get("/api/v1/leaves")
                        .param("status", "APPROVED")
                        .param("startDate", "2026-07-01")
                        .param("endDate", "2026-07-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].employeeName").value("홍길동"))
                .andExpect(jsonPath("$.data.content[0].status").value("APPROVED"))
                .andExpect(jsonPath("$.data.totalElements").value(1));
    }

    @Test
    @WithMockUser(username = LOGIN_ID)
    @DisplayName("관리자 잔여 목록 — 연도 미지정 시 올해로 위임")
    void get_balances_default_year() throws Exception {
        // given
        int currentYear = LocalDate.now().getYear();
        given(leaveService.getBalances(currentYear)).willReturn(List.of(new LeaveBalanceAdminResponse(
                1L, "홍길동", currentYear, new BigDecimal("15"), new BigDecimal("3"), new BigDecimal("12")
        )));

        // when & then
        mockMvc.perform(get("/api/v1/leaves/balances"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].employeeName").value("홍길동"))
                .andExpect(jsonPath("$.data[0].remainingDays").value(12));
        verify(leaveService).getBalances(currentYear);
    }

    @Test
    @WithMockUser(username = LOGIN_ID)
    @DisplayName("부여 일수 조정 성공")
    void change_granted_days_success() throws Exception {
        // when & then
        mockMvc.perform(put("/api/v1/leaves/balances/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new LeaveBalanceUpdateRequest(2026, new BigDecimal("20")))))
                .andExpect(status().isNoContent());
        verify(leaveService).changeGrantedDays(eq(1L), any());
    }

    @Test
    @WithMockUser(username = LOGIN_ID)
    @DisplayName("음수 부여 일수 조정 시 400")
    void change_granted_days_fail_negative() throws Exception {
        // when & then
        mockMvc.perform(put("/api/v1/leaves/balances/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new LeaveBalanceUpdateRequest(2026, new BigDecimal("-1")))))
                .andExpect(status().isBadRequest());
        verify(leaveService, never()).changeGrantedDays(any(), any());
    }
}
