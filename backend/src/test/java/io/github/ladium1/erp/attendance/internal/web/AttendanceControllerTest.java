package io.github.ladium1.erp.attendance.internal.web;

import io.github.ladium1.erp.attendance.internal.dto.AttendanceCorrectRequest;
import io.github.ladium1.erp.attendance.internal.dto.AttendanceResponse;
import io.github.ladium1.erp.attendance.internal.dto.CheckInRequest;
import io.github.ladium1.erp.attendance.internal.dto.CheckOutRequest;
import io.github.ladium1.erp.attendance.internal.exception.AttendanceErrorCode;
import io.github.ladium1.erp.attendance.internal.service.AttendanceService;
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
import java.time.LocalDateTime;
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

@WebMvcTest(AttendanceController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(AttendanceControllerTest.TestWebMvcConfig.class)
class AttendanceControllerTest {

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
    private static final double LATITUDE = 37.5663;
    private static final double LONGITUDE = 126.9779;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AttendanceService attendanceService;

    @MockitoBean
    private MenuPermissionEvaluator menuPermissionEvaluator;

    @BeforeEach
    void allow_all_permissions() {
        given(menuPermissionEvaluator.canRead(any(), any())).willReturn(true);
        given(menuPermissionEvaluator.canWrite(any(), any())).willReturn(true);
    }

    private AttendanceResponse attendanceResponse(LocalDateTime checkOutAt) {
        return new AttendanceResponse(
                1L, 1L, "홍길동", LocalDate.of(2026, 7, 6),
                LocalDateTime.of(2026, 7, 6, 9, 0), checkOutAt,
                true, checkOutAt != null
        );
    }

    @Test
    @WithMockUser(username = LOGIN_ID)
    @DisplayName("체크인 성공")
    void check_in_success() throws Exception {
        // given
        given(attendanceService.checkIn(any(), any())).willReturn(attendanceResponse(null));

        // when & then
        mockMvc.perform(post("/api/v1/attendances/check-in")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CheckInRequest(LATITUDE, LONGITUDE))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.employeeName").value("홍길동"))
                .andExpect(jsonPath("$.data.checkInWithinRange").value(true));
        verify(attendanceService).checkIn(eq(LOGIN_ID), any());
    }

    @Test
    @WithMockUser(username = LOGIN_ID)
    @DisplayName("좌표 없는 체크인 시 400")
    void check_in_fail_missing_latitude() throws Exception {
        // when & then
        mockMvc.perform(post("/api/v1/attendances/check-in")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CheckInRequest(null, LONGITUDE))))
                .andExpect(status().isBadRequest());
        verify(attendanceService, never()).checkIn(any(), any());
    }

    @Test
    @WithMockUser(username = LOGIN_ID)
    @DisplayName("중복 체크인 시 409")
    void check_in_fail_already_checked_in() throws Exception {
        // given
        given(attendanceService.checkIn(any(), any()))
                .willThrow(new BusinessException(AttendanceErrorCode.ALREADY_CHECKED_IN));

        // when & then
        mockMvc.perform(post("/api/v1/attendances/check-in")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CheckInRequest(LATITUDE, LONGITUDE))))
                .andExpect(status().isConflict());
    }

    @Test
    @WithMockUser(username = LOGIN_ID)
    @DisplayName("체크아웃 성공")
    void check_out_success() throws Exception {
        // given
        given(attendanceService.checkOut(any(), any()))
                .willReturn(attendanceResponse(LocalDateTime.of(2026, 7, 6, 18, 0)));

        // when & then
        mockMvc.perform(post("/api/v1/attendances/check-out")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CheckOutRequest(LATITUDE, LONGITUDE))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.checkOutWithinRange").value(true));
        verify(attendanceService).checkOut(eq(LOGIN_ID), any());
    }

    @Test
    @WithMockUser(username = LOGIN_ID)
    @DisplayName("체크인 전 체크아웃 시 409")
    void check_out_fail_not_checked_in_yet() throws Exception {
        // given
        given(attendanceService.checkOut(any(), any()))
                .willThrow(new BusinessException(AttendanceErrorCode.NOT_CHECKED_IN_YET));

        // when & then
        mockMvc.perform(post("/api/v1/attendances/check-out")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CheckOutRequest(LATITUDE, LONGITUDE))))
                .andExpect(status().isConflict());
    }

    @Test
    @WithMockUser(username = LOGIN_ID)
    @DisplayName("내 월별 근태 조회 성공")
    void get_my_monthly_success() throws Exception {
        // given
        given(attendanceService.getMyMonthly(LOGIN_ID, 2026, 7))
                .willReturn(List.of(attendanceResponse(LocalDateTime.of(2026, 7, 6, 18, 0))));

        // when & then
        mockMvc.perform(get("/api/v1/attendances/me")
                        .param("year", "2026")
                        .param("month", "7"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].employeeName").value("홍길동"))
                .andExpect(jsonPath("$.data[0].workDate").value("2026-07-06"));
    }

    @Test
    @WithMockUser(username = LOGIN_ID)
    @DisplayName("전 직원 근태 현황 조회 성공")
    void search_success() throws Exception {
        // given
        PageResponse<AttendanceResponse> page = new PageResponse<>(
                List.of(attendanceResponse(null)), 0, 20, 1, 1, false
        );
        given(attendanceService.search(any(), any())).willReturn(page);

        // when & then
        mockMvc.perform(get("/api/v1/attendances")
                        .param("year", "2026")
                        .param("month", "7"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].employeeName").value("홍길동"))
                .andExpect(jsonPath("$.data.totalElements").value(1));
    }

    @Test
    @WithMockUser(username = LOGIN_ID)
    @DisplayName("근태 정정 성공")
    void correct_success() throws Exception {
        // given
        given(attendanceService.correct(eq(1L), any()))
                .willReturn(attendanceResponse(LocalDateTime.of(2026, 7, 6, 18, 0)));

        // when & then
        mockMvc.perform(put("/api/v1/attendances/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new AttendanceCorrectRequest(
                                LocalDateTime.of(2026, 7, 6, 9, 0), LocalDateTime.of(2026, 7, 6, 18, 0)))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.employeeName").value("홍길동"));
        verify(attendanceService).correct(eq(1L), any());
    }

    @Test
    @WithMockUser(username = LOGIN_ID)
    @DisplayName("시각 없는 근태 정정 시 400")
    void correct_fail_no_fields() throws Exception {
        // when & then
        mockMvc.perform(put("/api/v1/attendances/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new AttendanceCorrectRequest(null, null))))
                .andExpect(status().isBadRequest());
        verify(attendanceService, never()).correct(any(), any());
    }

    @Test
    @WithMockUser(username = LOGIN_ID)
    @DisplayName("존재하지 않는 근태 정정 시 404")
    void correct_fail_not_found() throws Exception {
        // given
        given(attendanceService.correct(eq(99L), any()))
                .willThrow(new BusinessException(AttendanceErrorCode.ATTENDANCE_NOT_FOUND));

        // when & then
        mockMvc.perform(put("/api/v1/attendances/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new AttendanceCorrectRequest(
                                LocalDateTime.of(2026, 7, 6, 9, 0), null))))
                .andExpect(status().isNotFound());
    }
}
