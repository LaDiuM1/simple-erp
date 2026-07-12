package io.github.ladium1.erp.afterservice.internal.web;

import io.github.ladium1.erp.afterservice.internal.dto.AfterServiceCreateRequest;
import io.github.ladium1.erp.afterservice.internal.dto.AfterServiceDetailResponse;
import io.github.ladium1.erp.afterservice.internal.dto.AfterServiceSearchCondition;
import io.github.ladium1.erp.afterservice.internal.dto.AfterServiceSummaryResponse;
import io.github.ladium1.erp.afterservice.internal.dto.AfterServiceUpdateRequest;
import io.github.ladium1.erp.afterservice.internal.dto.ServiceExpenseRequest;
import io.github.ladium1.erp.afterservice.internal.dto.ServiceVisitRequest;
import io.github.ladium1.erp.afterservice.internal.entity.ExpensePayerType;
import io.github.ladium1.erp.afterservice.internal.entity.ServiceExpenseCategory;
import io.github.ladium1.erp.afterservice.internal.entity.ServiceStatus;
import io.github.ladium1.erp.afterservice.internal.entity.ServiceType;
import io.github.ladium1.erp.afterservice.internal.entity.WarrantyDecision;
import io.github.ladium1.erp.afterservice.internal.exception.AfterServiceErrorCode;
import io.github.ladium1.erp.afterservice.internal.service.AfterServiceService;
import io.github.ladium1.erp.global.exception.BusinessException;
import io.github.ladium1.erp.global.security.MenuPermissionEvaluator;
import io.github.ladium1.erp.global.web.PageResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
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

@WebMvcTest(AfterServiceController.class)
@AutoConfigureMockMvc(addFilters = false)
class AfterServiceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AfterServiceService afterServiceService;

    @MockitoBean
    private MenuPermissionEvaluator menuPermissionEvaluator;

    @BeforeEach
    void allow_all_permissions() {
        given(menuPermissionEvaluator.canRead(any(), any())).willReturn(true);
        given(menuPermissionEvaluator.canWrite(any(), any())).willReturn(true);
    }

    @Test
    @DisplayName("AS 목록 검색 성공")
    void search_success() throws Exception {
        // given
        AfterServiceSummaryResponse summary = AfterServiceSummaryResponse.builder()
                .id(1L).receiptNo("AS2026-0001").customerName("대성상사")
                .equipmentModelName("HLA-1530").type(ServiceType.REPAIR)
                .status(ServiceStatus.ASSIGNED).warrantyDecision(WarrantyDecision.UNDECIDED)
                .expenseTotal(850_000L)
                .receivedDate(LocalDate.of(2026, 5, 1))
                .build();
        PageResponse<AfterServiceSummaryResponse> page = new PageResponse<>(
                List.of(summary), 0, 20, 1, 1, false
        );
        given(afterServiceService.search(any(), any())).willReturn(page);

        // when & then
        mockMvc.perform(get("/api/v1/after-services"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].receiptNo").value("AS2026-0001"))
                .andExpect(jsonPath("$.data.content[0].expenseTotal").value(850_000L))
                .andExpect(jsonPath("$.data.totalElements").value(1));
    }

    @Test
    @DisplayName("검색 조건 바인딩 — 유형 / 상태 / 기간 필터가 condition 으로 전달")
    void search_condition_binding() throws Exception {
        // given
        given(afterServiceService.search(any(), any()))
                .willReturn(new PageResponse<>(List.of(), 0, 20, 0, 0, false));

        // when
        mockMvc.perform(get("/api/v1/after-services")
                        .param("type", "REPAIR")
                        .param("status", "IN_PROGRESS")
                        .param("receivedDateFrom", "2026-01-01"))
                .andExpect(status().isOk());

        // then
        ArgumentCaptor<AfterServiceSearchCondition> captor = ArgumentCaptor.forClass(AfterServiceSearchCondition.class);
        verify(afterServiceService).search(captor.capture(), any());
        assertThat(captor.getValue().type()).isEqualTo(ServiceType.REPAIR);
        assertThat(captor.getValue().status()).isEqualTo(ServiceStatus.IN_PROGRESS);
        assertThat(captor.getValue().receivedDateFrom()).isEqualTo(LocalDate.of(2026, 1, 1));
    }

    @Test
    @DisplayName("AS 상세 조회 성공")
    void get_detail_success() throws Exception {
        // given
        AfterServiceDetailResponse detail = AfterServiceDetailResponse.builder()
                .id(1L).receiptNo("AS2026-0001").customerName("대성상사")
                .status(ServiceStatus.COMPLETED).warrantyDecision(WarrantyDecision.FREE)
                .expenseTotal(850_000L)
                .visits(List.of()).expenses(List.of())
                .build();
        given(afterServiceService.getDetail(1L)).willReturn(detail);

        // when & then
        mockMvc.perform(get("/api/v1/after-services/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.receiptNo").value("AS2026-0001"))
                .andExpect(jsonPath("$.data.expenseTotal").value(850_000L));
    }

    @Test
    @DisplayName("존재하지 않는 AS 건 조회 시 404")
    void get_detail_fail_not_found() throws Exception {
        // given
        given(afterServiceService.getDetail(99L))
                .willThrow(new BusinessException(AfterServiceErrorCode.AFTER_SERVICE_NOT_FOUND));

        // when & then
        mockMvc.perform(get("/api/v1/after-services/{id}", 99L))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("AS 접수 성공")
    void create_success() throws Exception {
        // given
        given(afterServiceService.create(any())).willReturn(42L);

        // when & then
        mockMvc.perform(post("/api/v1/after-services")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(baseCreateRequest())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(42));
    }

    @Test
    @DisplayName("고객사 미입력 접수 시 400")
    void create_fail_missing_customer() throws Exception {
        // given
        AfterServiceCreateRequest request = new AfterServiceCreateRequest(
                null, null, null, LocalDate.of(2026, 5, 1),
                ServiceType.REPAIR, null, ServiceStatus.RECEIVED,
                null, WarrantyDecision.UNDECIDED, null, null
        );

        // when & then
        mockMvc.perform(post("/api/v1/after-services")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("AS 수정 성공")
    void update_success() throws Exception {
        // given
        AfterServiceUpdateRequest request = new AfterServiceUpdateRequest(
                1L, null, LocalDate.of(2026, 5, 1), ServiceType.REPAIR, "출력 저하",
                ServiceStatus.COMPLETED, 5L, WarrantyDecision.FREE, null, LocalDate.of(2026, 5, 3)
        );

        // when & then
        mockMvc.perform(put("/api/v1/after-services/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNoContent());
        verify(afterServiceService).update(eq(1L), any());
    }

    @Test
    @DisplayName("AS 삭제 / 일괄 삭제 성공")
    void delete_success() throws Exception {
        mockMvc.perform(delete("/api/v1/after-services/{id}", 1L))
                .andExpect(status().isNoContent());
        verify(afterServiceService).delete(1L);

        mockMvc.perform(delete("/api/v1/after-services")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(List.of(1L, 2L))))
                .andExpect(status().isNoContent());
        verify(afterServiceService).deleteAll(List.of(1L, 2L));
    }

    @Test
    @DisplayName("방문 일지 등록 성공")
    void create_visit_success() throws Exception {
        // given
        ServiceVisitRequest request = new ServiceVisitRequest(
                LocalDate.of(2026, 5, 2), 5L, "출력 저하", "보호창 교체");
        given(afterServiceService.createVisit(eq(1L), any())).willReturn(10L);

        // when & then
        mockMvc.perform(post("/api/v1/after-services/{afterServiceId}/visits", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(10));
    }

    @Test
    @DisplayName("방문 일지 엔지니어 미입력 시 400")
    void create_visit_fail_missing_engineer() throws Exception {
        // given
        ServiceVisitRequest request = new ServiceVisitRequest(LocalDate.of(2026, 5, 2), null, null, null);

        // when & then
        mockMvc.perform(post("/api/v1/after-services/{afterServiceId}/visits", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("방문 일지 수정 / 삭제 성공")
    void update_and_delete_visit_success() throws Exception {
        // given
        ServiceVisitRequest request = new ServiceVisitRequest(
                LocalDate.of(2026, 5, 3), 5L, "재점검", "정상 확인");

        // when & then
        mockMvc.perform(put("/api/v1/after-services/visits/{id}", 10L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNoContent());
        verify(afterServiceService).updateVisit(eq(10L), any());

        mockMvc.perform(delete("/api/v1/after-services/visits/{id}", 10L))
                .andExpect(status().isNoContent());
        verify(afterServiceService).deleteVisit(10L);
    }

    @Test
    @DisplayName("경비 등록 성공")
    void create_expense_success() throws Exception {
        // given
        ServiceExpenseRequest request = new ServiceExpenseRequest(
                ServiceExpenseCategory.LODGING, 150_000L, ExpensePayerType.COMPANY,
                LocalDate.of(2026, 5, 2), 5L, "김포 호텔");
        given(afterServiceService.createExpense(eq(1L), any())).willReturn(20L);

        // when & then
        mockMvc.perform(post("/api/v1/after-services/{afterServiceId}/expenses", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(20));
    }

    @Test
    @DisplayName("경비 금액 미입력 시 400")
    void create_expense_fail_missing_amount() throws Exception {
        // given
        ServiceExpenseRequest request = new ServiceExpenseRequest(
                ServiceExpenseCategory.MEAL, null, ExpensePayerType.ENGINEER, null, null, null);

        // when & then
        mockMvc.perform(post("/api/v1/after-services/{afterServiceId}/expenses", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("경비 수정 / 삭제 성공")
    void update_and_delete_expense_success() throws Exception {
        // given
        ServiceExpenseRequest request = new ServiceExpenseRequest(
                ServiceExpenseCategory.MEAL, 30_000L, ExpensePayerType.ENGINEER, null, 5L, null);

        // when & then
        mockMvc.perform(put("/api/v1/after-services/expenses/{id}", 20L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNoContent());
        verify(afterServiceService).updateExpense(eq(20L), any());

        mockMvc.perform(delete("/api/v1/after-services/expenses/{id}", 20L))
                .andExpect(status().isNoContent());
        verify(afterServiceService).deleteExpense(20L);
    }

    private AfterServiceCreateRequest baseCreateRequest() {
        return new AfterServiceCreateRequest(
                null, 1L, 10L, LocalDate.of(2026, 5, 1),
                ServiceType.REPAIR, "레이저 출력 저하", ServiceStatus.RECEIVED,
                null, WarrantyDecision.UNDECIDED, null, null
        );
    }
}
