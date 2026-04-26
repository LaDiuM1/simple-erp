package io.github.ladium1.erp.salescustomer.internal.web;

import io.github.ladium1.erp.global.exception.BusinessException;
import io.github.ladium1.erp.global.security.MenuPermissionEvaluator;
import io.github.ladium1.erp.salescustomer.internal.dto.SalesActivityCreateRequest;
import io.github.ladium1.erp.salescustomer.internal.dto.SalesActivityUpdateRequest;
import io.github.ladium1.erp.salescustomer.internal.dto.SalesAssignmentCreateRequest;
import io.github.ladium1.erp.salescustomer.internal.dto.SalesAssignmentTerminateRequest;
import io.github.ladium1.erp.salescustomer.internal.dto.SalesAssignmentUpdateRequest;
import io.github.ladium1.erp.salescustomer.internal.dto.SalesCustomerAggregate;
import io.github.ladium1.erp.salescustomer.internal.dto.SalesCustomerDetailResponse;
import io.github.ladium1.erp.salescustomer.internal.entity.SalesActivityType;
import io.github.ladium1.erp.salescustomer.internal.exception.SalesCustomerErrorCode;
import io.github.ladium1.erp.salescustomer.internal.service.SalesCustomerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDate;
import java.time.LocalDateTime;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SalesCustomerController.class)
@AutoConfigureMockMvc(addFilters = false)
class SalesCustomerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private SalesCustomerService salesCustomerService;

    @MockitoBean
    private MenuPermissionEvaluator menuPermissionEvaluator;

    @BeforeEach
    void allow_all_permissions() {
        given(menuPermissionEvaluator.canRead(any(), any())).willReturn(true);
        given(menuPermissionEvaluator.canWrite(any(), any())).willReturn(true);
    }

    @Test
    @DisplayName("aggregates 조회 성공")
    void aggregates_success() throws Exception {
        // given
        SalesCustomerAggregate agg = SalesCustomerAggregate.builder()
                .customerId(1L)
                .primaryAssigneeId(10L)
                .primaryAssigneeName("김주담")
                .activeAssigneeCount(2L)
                .activityCount(5L)
                .lastActivityDate(LocalDateTime.of(2026, 4, 20, 10, 0))
                .build();
        given(salesCustomerService.aggregateByCustomerIds(List.of(1L, 2L)))
                .willReturn(List.of(agg));

        // when & then
        mockMvc.perform(get("/api/v1/sales-customers/aggregates")
                        .param("customerIds", "1", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].customerId").value(1))
                .andExpect(jsonPath("$.data[0].primaryAssigneeName").value("김주담"))
                .andExpect(jsonPath("$.data[0].activityCount").value(5));
    }

    @Test
    @DisplayName("상세 조회 성공")
    void get_detail_success() throws Exception {
        // given
        SalesCustomerDetailResponse detail = SalesCustomerDetailResponse.builder()
                .customerId(1L)
                .customerCode("C0001")
                .customerName("대성상사")
                .activities(List.of())
                .assignments(List.of())
                .build();
        given(salesCustomerService.getDetail(1L)).willReturn(detail);

        // when & then
        mockMvc.perform(get("/api/v1/sales-customers/{customerId}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.customerName").value("대성상사"));
    }

    @Test
    @DisplayName("활동 등록 성공")
    void create_activity_success() throws Exception {
        SalesActivityCreateRequest request = new SalesActivityCreateRequest(
                1L, SalesActivityType.VISIT, LocalDateTime.now(),
                "미팅", "내용", 10L, "고객 담당", "팀장");
        given(salesCustomerService.createActivity(any())).willReturn(42L);

        mockMvc.perform(post("/api/v1/sales-customers/activities")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(42));
    }

    @Test
    @DisplayName("존재하지 않는 활동 수정 시 404")
    void update_activity_fail_not_found() throws Exception {
        SalesActivityUpdateRequest request = new SalesActivityUpdateRequest(
                SalesActivityType.CALL, LocalDateTime.now(),
                "수정", "내용", 10L, null, null);
        willThrow(new BusinessException(SalesCustomerErrorCode.ACTIVITY_NOT_FOUND))
                .given(salesCustomerService).updateActivity(eq(99L), any());

        mockMvc.perform(put("/api/v1/sales-customers/activities/{id}", 99L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("활동 삭제 성공")
    void delete_activity_success() throws Exception {
        mockMvc.perform(delete("/api/v1/sales-customers/activities/{id}", 7L))
                .andExpect(status().isNoContent());
        verify(salesCustomerService).deleteActivity(7L);
    }

    @Test
    @DisplayName("배정 등록 성공")
    void create_assignment_success() throws Exception {
        SalesAssignmentCreateRequest request = new SalesAssignmentCreateRequest(
                1L, 10L, LocalDate.of(2026, 4, 1), true, "신규 주담당");
        given(salesCustomerService.createAssignment(any())).willReturn(50L);

        mockMvc.perform(post("/api/v1/sales-customers/assignments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(50));
    }

    @Test
    @DisplayName("배정 수정 성공")
    void update_assignment_success() throws Exception {
        SalesAssignmentUpdateRequest request = new SalesAssignmentUpdateRequest(
                LocalDate.of(2026, 4, 15), false, "보조로 변경");

        mockMvc.perform(put("/api/v1/sales-customers/assignments/{id}", 7L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNoContent());
        verify(salesCustomerService).updateAssignment(eq(7L), any());
    }

    @Test
    @DisplayName("배정 종료 성공")
    void terminate_assignment_success() throws Exception {
        SalesAssignmentTerminateRequest request = new SalesAssignmentTerminateRequest(
                LocalDate.of(2026, 4, 30), "이직");

        mockMvc.perform(put("/api/v1/sales-customers/assignments/{id}/terminate", 7L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNoContent());
        verify(salesCustomerService).terminateAssignment(eq(7L), any());
    }

    @Test
    @DisplayName("배정 종료 실패 — 이미 종료된 배정 (400)")
    void terminate_assignment_fail_already_terminated() throws Exception {
        SalesAssignmentTerminateRequest request = new SalesAssignmentTerminateRequest(
                LocalDate.of(2026, 4, 30), "재시도");
        willThrow(new BusinessException(SalesCustomerErrorCode.ASSIGNMENT_ALREADY_TERMINATED))
                .given(salesCustomerService).terminateAssignment(eq(7L), any());

        mockMvc.perform(put("/api/v1/sales-customers/assignments/{id}/terminate", 7L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("배정 삭제 성공")
    void delete_assignment_success() throws Exception {
        mockMvc.perform(delete("/api/v1/sales-customers/assignments/{id}", 7L))
                .andExpect(status().isNoContent());
        verify(salesCustomerService).deleteAssignment(7L);
    }
}
