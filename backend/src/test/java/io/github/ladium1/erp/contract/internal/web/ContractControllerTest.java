package io.github.ladium1.erp.contract.internal.web;

import io.github.ladium1.erp.contract.internal.dto.ContractCreateRequest;
import io.github.ladium1.erp.contract.internal.dto.ContractDetailResponse;
import io.github.ladium1.erp.contract.internal.dto.ContractNoteCreateRequest;
import io.github.ladium1.erp.contract.internal.dto.ContractPaymentRequest;
import io.github.ladium1.erp.contract.internal.dto.ContractSearchCondition;
import io.github.ladium1.erp.contract.internal.dto.ContractSummaryResponse;
import io.github.ladium1.erp.contract.internal.dto.ContractUpdateRequest;
import io.github.ladium1.erp.contract.internal.entity.ContractStatus;
import io.github.ladium1.erp.contract.internal.entity.SupportProgramStatus;
import io.github.ladium1.erp.contract.internal.exception.ContractErrorCode;
import io.github.ladium1.erp.contract.internal.service.ContractService;
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

@WebMvcTest(ContractController.class)
@AutoConfigureMockMvc(addFilters = false)
class ContractControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ContractService contractService;

    @MockitoBean
    private MenuPermissionEvaluator menuPermissionEvaluator;

    @BeforeEach
    void allow_all_permissions() {
        given(menuPermissionEvaluator.canRead(any(), any())).willReturn(true);
        given(menuPermissionEvaluator.canWrite(any(), any())).willReturn(true);
    }

    @Test
    @DisplayName("계약 목록 검색 성공")
    void search_success() throws Exception {
        // given
        ContractSummaryResponse summary = ContractSummaryResponse.builder()
                .id(1L).contractNo("CT2026-001").customerName("대성상사").employeeName("김영업")
                .supplierName("YAWEI").productModelName("HLA-1530").categoryName("평판 레이저")
                .finalAmount(100_000_000L).outstandingAmount(70_000_000L)
                .contractDate(LocalDate.of(2026, 1, 10))
                .supportProgramStatus(SupportProgramStatus.NONE)
                .status(ContractStatus.CONTRACTED)
                .build();
        PageResponse<ContractSummaryResponse> page = new PageResponse<>(
                List.of(summary), 0, 20, 1, 1, false
        );
        given(contractService.search(any(), any())).willReturn(page);

        // when & then
        mockMvc.perform(get("/api/v1/contracts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].contractNo").value("CT2026-001"))
                .andExpect(jsonPath("$.data.content[0].customerName").value("대성상사"))
                .andExpect(jsonPath("$.data.content[0].outstandingAmount").value(70_000_000L))
                .andExpect(jsonPath("$.data.totalElements").value(1));
    }

    @Test
    @DisplayName("검색 조건 바인딩 — 상태 / 기간 필터가 condition 으로 전달")
    void search_condition_binding() throws Exception {
        // given
        given(contractService.search(any(), any()))
                .willReturn(new PageResponse<>(List.of(), 0, 20, 0, 0, false));

        // when
        mockMvc.perform(get("/api/v1/contracts")
                        .param("status", "ORDERED")
                        .param("contractDateFrom", "2026-01-01")
                        .param("contractDateTo", "2026-06-30"))
                .andExpect(status().isOk());

        // then
        ArgumentCaptor<ContractSearchCondition> captor = ArgumentCaptor.forClass(ContractSearchCondition.class);
        verify(contractService).search(captor.capture(), any());
        assertThat(captor.getValue().status()).isEqualTo(ContractStatus.ORDERED);
        assertThat(captor.getValue().contractDateFrom()).isEqualTo(LocalDate.of(2026, 1, 1));
        assertThat(captor.getValue().contractDateTo()).isEqualTo(LocalDate.of(2026, 6, 30));
    }

    @Test
    @DisplayName("계약 상세 조회 성공")
    void get_detail_success() throws Exception {
        // given
        ContractDetailResponse detail = ContractDetailResponse.builder()
                .id(1L).contractNo("CT2026-001").customerName("대성상사")
                .finalAmount(100_000_000L).paidTotal(30_000_000L).outstandingAmount(70_000_000L)
                .status(ContractStatus.CONTRACTED)
                .payments(List.of()).notes(List.of())
                .build();
        given(contractService.getDetail(1L)).willReturn(detail);

        // when & then
        mockMvc.perform(get("/api/v1/contracts/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.contractNo").value("CT2026-001"))
                .andExpect(jsonPath("$.data.paidTotal").value(30_000_000L));
    }

    @Test
    @DisplayName("존재하지 않는 계약 조회 시 404")
    void get_detail_fail_not_found() throws Exception {
        // given
        given(contractService.getDetail(99L))
                .willThrow(new BusinessException(ContractErrorCode.CONTRACT_NOT_FOUND));

        // when & then
        mockMvc.perform(get("/api/v1/contracts/{id}", 99L))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("계약 등록 성공")
    void create_success() throws Exception {
        // given
        given(contractService.create(any())).willReturn(42L);

        // when & then
        mockMvc.perform(post("/api/v1/contracts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(baseCreateRequest())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(42));
    }

    @Test
    @DisplayName("고객사 미입력 등록 시 400")
    void create_fail_missing_customer() throws Exception {
        // given
        ContractCreateRequest request = new ContractCreateRequest(
                null, null, 2L, 3L,
                null, null, null,
                null, 100_000_000L, null, null, SupportProgramStatus.NONE,
                LocalDate.of(2026, 1, 10), null, null, null, null, null, null,
                null, ContractStatus.CONTRACTED
        );

        // when & then
        mockMvc.perform(post("/api/v1/contracts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("중복 계약 번호 등록 시 409")
    void create_fail_duplicate_contract_no() throws Exception {
        // given
        willThrow(new BusinessException(ContractErrorCode.DUPLICATE_CONTRACT_NO))
                .given(contractService).create(any());

        // when & then
        mockMvc.perform(post("/api/v1/contracts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(baseCreateRequest())))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("계약 수정 성공")
    void update_success() throws Exception {
        // given
        ContractUpdateRequest request = new ContractUpdateRequest(
                1L, 2L, 3L,
                null, null, null,
                null, 120_000_000L, null, null, SupportProgramStatus.NONE,
                LocalDate.of(2026, 1, 10), null, null, null, null, null, null,
                null, ContractStatus.ORDERED
        );

        // when & then
        mockMvc.perform(put("/api/v1/contracts/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNoContent());
        verify(contractService).update(eq(1L), any());
    }

    @Test
    @DisplayName("계약 삭제 성공")
    void delete_success() throws Exception {
        // when & then
        mockMvc.perform(delete("/api/v1/contracts/{id}", 1L))
                .andExpect(status().isNoContent());
        verify(contractService).delete(1L);
    }

    @Test
    @DisplayName("계약 일괄 삭제 성공")
    void delete_all_success() throws Exception {
        // when & then
        mockMvc.perform(delete("/api/v1/contracts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(List.of(1L, 2L))))
                .andExpect(status().isNoContent());
        verify(contractService).deleteAll(List.of(1L, 2L));
    }

    @Test
    @DisplayName("대금 회차 등록 성공")
    void create_payment_success() throws Exception {
        // given
        ContractPaymentRequest request = new ContractPaymentRequest(
                "계약금", LocalDate.of(2026, 1, 15), 30_000_000L,
                null, null, null, null, null);
        given(contractService.createPayment(eq(1L), any())).willReturn(10L);

        // when & then
        mockMvc.perform(post("/api/v1/contracts/{contractId}/payments", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(10));
    }

    @Test
    @DisplayName("대금 회차 라벨 미입력 시 400")
    void create_payment_fail_missing_label() throws Exception {
        // given
        ContractPaymentRequest request = new ContractPaymentRequest(
                " ", null, null, null, null, null, null, null);

        // when & then
        mockMvc.perform(post("/api/v1/contracts/{contractId}/payments", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("대금 회차 수정 / 삭제 성공")
    void update_and_delete_payment_success() throws Exception {
        // given
        ContractPaymentRequest request = new ContractPaymentRequest(
                "잔금", null, null, LocalDate.of(2026, 3, 2), 70_000_000L, null, null, null);

        // when & then
        mockMvc.perform(put("/api/v1/contracts/payments/{id}", 10L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNoContent());
        verify(contractService).updatePayment(eq(10L), any());

        mockMvc.perform(delete("/api/v1/contracts/payments/{id}", 10L))
                .andExpect(status().isNoContent());
        verify(contractService).deletePayment(10L);
    }

    @Test
    @DisplayName("계약 메모 등록 성공")
    void create_note_success() throws Exception {
        // given
        given(contractService.createNote(eq(1L), any())).willReturn(20L);

        // when & then
        mockMvc.perform(post("/api/v1/contracts/{contractId}/notes", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new ContractNoteCreateRequest("납기 1주 연기"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(20));
    }

    @Test
    @DisplayName("계약 메모 내용 미입력 시 400")
    void create_note_fail_missing_content() throws Exception {
        mockMvc.perform(post("/api/v1/contracts/{contractId}/notes", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new ContractNoteCreateRequest(" "))))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("계약 메모 삭제 성공")
    void delete_note_success() throws Exception {
        mockMvc.perform(delete("/api/v1/contracts/notes/{id}", 20L))
                .andExpect(status().isNoContent());
        verify(contractService).deleteNote(20L);
    }

    private ContractCreateRequest baseCreateRequest() {
        return new ContractCreateRequest(
                null, 1L, 2L, 3L,
                null, null, null,
                null, 100_000_000L, null, null, SupportProgramStatus.NONE,
                LocalDate.of(2026, 1, 10), null, null, null, null, null, null,
                null, ContractStatus.CONTRACTED
        );
    }
}
