package io.github.ladium1.erp.expense.internal.web;

import io.github.ladium1.erp.expense.internal.dto.ExpenseCreateRequest;
import io.github.ladium1.erp.expense.internal.dto.ExpenseDetailResponse;
import io.github.ladium1.erp.expense.internal.dto.ExpenseReceiptDownload;
import io.github.ladium1.erp.expense.internal.dto.ExpenseSearchScope;
import io.github.ladium1.erp.expense.internal.dto.ExpenseSummaryResponse;
import io.github.ladium1.erp.expense.internal.entity.ExpenseCategory;
import io.github.ladium1.erp.expense.internal.entity.ExpenseStatus;
import io.github.ladium1.erp.expense.internal.exception.ExpenseErrorCode;
import io.github.ladium1.erp.expense.internal.service.ExpenseService;
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
import org.springframework.http.HttpHeaders;
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
import java.util.stream.IntStream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ExpenseController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(ExpenseControllerTest.TestWebMvcConfig.class)
class ExpenseControllerTest {

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

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ExpenseService expenseService;

    @MockitoBean
    private MenuPermissionEvaluator menuPermissionEvaluator;

    @BeforeEach
    void allow_all_permissions() {
        given(menuPermissionEvaluator.canRead(any(), any())).willReturn(true);
        given(menuPermissionEvaluator.canWrite(any(), any())).willReturn(true);
    }

    private ExpenseCreateRequest createRequest(String title, List<ExpenseCreateRequest.ItemRequest> items) {
        return new ExpenseCreateRequest(title, items, List.of(5L));
    }

    private ExpenseCreateRequest.ItemRequest item() {
        return new ExpenseCreateRequest.ItemRequest(
                LocalDate.of(2026, 6, 1), ExpenseCategory.TRANSPORT,
                new BigDecimal("12000"), "KTX 왕복", null
        );
    }

    @Test
    @WithMockUser(username = LOGIN_ID)
    @DisplayName("경비 청구 등록 성공")
    void create_success() throws Exception {
        // given
        ExpenseCreateRequest request = createRequest("6월 출장 경비", List.of(item()));
        given(expenseService.create(any(), any())).willReturn(42L);

        // when & then
        mockMvc.perform(post("/api/v1/expenses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(42));
        verify(expenseService).create(eq(LOGIN_ID), any());
    }

    @Test
    @WithMockUser(username = LOGIN_ID)
    @DisplayName("제목 없는 등록 시 400")
    void create_fail_blank_title() throws Exception {
        // given
        ExpenseCreateRequest request = createRequest("", List.of(item()));

        // when & then
        mockMvc.perform(post("/api/v1/expenses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
        verify(expenseService, never()).create(any(), any());
    }

    @Test
    @WithMockUser(username = LOGIN_ID)
    @DisplayName("빈 항목 등록 시 400")
    void create_fail_empty_items() throws Exception {
        // given
        ExpenseCreateRequest request = createRequest("빈 청구", List.of());
        willThrow(new BusinessException(ExpenseErrorCode.EMPTY_ITEMS))
                .given(expenseService).create(eq(LOGIN_ID), any());

        // when & then
        mockMvc.perform(post("/api/v1/expenses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = LOGIN_ID)
    @DisplayName("경비 항목 50개 초과 등록 시 400")
    void create_fail_too_many_items() throws Exception {
        ExpenseCreateRequest request = createRequest(
                "과도한 항목",
                IntStream.range(0, 51).mapToObj(ignored -> item()).toList()
        );

        mockMvc.perform(post("/api/v1/expenses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
        verify(expenseService, never()).create(any(), any());
    }

    @Test
    @WithMockUser(username = LOGIN_ID)
    @DisplayName("DB 정밀도를 벗어난 경비 금액 등록 시 400")
    void create_fail_amount_exceeds_precision() throws Exception {
        ExpenseCreateRequest.ItemRequest oversized = new ExpenseCreateRequest.ItemRequest(
                LocalDate.of(2026, 6, 1), ExpenseCategory.TRANSPORT,
                new BigDecimal("1E+13"), "정밀도 초과", null
        );

        mockMvc.perform(post("/api/v1/expenses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest("금액 초과", List.of(oversized)))))
                .andExpect(status().isBadRequest());
        verify(expenseService, never()).create(any(), any());
    }

    @Test
    @WithMockUser(username = LOGIN_ID)
    @DisplayName("경비 항목 설명 255자 초과 등록 시 400")
    void create_fail_description_too_long() throws Exception {
        ExpenseCreateRequest.ItemRequest oversized = new ExpenseCreateRequest.ItemRequest(
                LocalDate.of(2026, 6, 1), ExpenseCategory.TRANSPORT,
                new BigDecimal("12000"), "가".repeat(256), null
        );

        mockMvc.perform(post("/api/v1/expenses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest("설명 초과", List.of(oversized)))))
                .andExpect(status().isBadRequest());
        verify(expenseService, never()).create(any(), any());
    }

    @Test
    @WithMockUser(username = LOGIN_ID)
    @DisplayName("내 경비 청구 목록 조회 성공")
    void search_success() throws Exception {
        // given
        ExpenseSummaryResponse summary = ExpenseSummaryResponse.builder()
                .id(1L).title("6월 출장 경비").totalAmount(new BigDecimal("100000"))
                .status(ExpenseStatus.IN_PROGRESS).claimantName("홍길동")
                .approvalDocumentId(99L).itemCount(3)
                .build();
        PageResponse<ExpenseSummaryResponse> page = new PageResponse<>(
                List.of(summary), 0, 20, 1, 1, false
        );
        given(expenseService.search(any(), any(), any(), any())).willReturn(page);

        // when & then
        mockMvc.perform(get("/api/v1/expenses"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].title").value("6월 출장 경비"))
                .andExpect(jsonPath("$.data.content[0].claimantName").value("홍길동"))
                .andExpect(jsonPath("$.data.content[0].itemCount").value(3))
                .andExpect(jsonPath("$.data.totalElements").value(1));
        verify(expenseService).search(eq(LOGIN_ID), eq(ExpenseSearchScope.MINE), any(), any());
    }

    @Test
    @WithMockUser(username = LOGIN_ID)
    @DisplayName("scope=ALL 파라미터 서비스 전달")
    void search_forwards_all_scope() throws Exception {
        // given
        PageResponse<ExpenseSummaryResponse> page = new PageResponse<>(List.of(), 0, 20, 0, 0, false);
        given(expenseService.search(any(), any(), any(), any())).willReturn(page);

        // when & then
        mockMvc.perform(get("/api/v1/expenses").param("scope", "ALL"))
                .andExpect(status().isOk());
        verify(expenseService).search(eq(LOGIN_ID), eq(ExpenseSearchScope.ALL), any(), any());
    }

    @Test
    @WithMockUser(username = LOGIN_ID)
    @DisplayName("경비 청구 상세 조회 성공")
    void get_detail_success() throws Exception {
        // given
        ExpenseDetailResponse detail = ExpenseDetailResponse.builder()
                .id(7L).title("6월 출장 경비").totalAmount(new BigDecimal("12000"))
                .status(ExpenseStatus.IN_PROGRESS)
                .claimantName("홍길동").approvalDocumentId(99L)
                .items(List.of(ExpenseDetailResponse.ItemResponse.builder()
                        .id(1L).expenseDate(LocalDate.of(2026, 6, 1))
                        .category(ExpenseCategory.TRANSPORT)
                        .amount(new BigDecimal("12000")).description("KTX 왕복")
                        .build()))
                .build();
        given(expenseService.getDetail(any(), eq(7L))).willReturn(detail);

        // when & then
        mockMvc.perform(get("/api/v1/expenses/{id}", 7L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.title").value("6월 출장 경비"))
                .andExpect(jsonPath("$.data.claimantName").value("홍길동"))
                .andExpect(jsonPath("$.data.items[0].category").value("TRANSPORT"));
    }

    @Test
    @WithMockUser(username = LOGIN_ID)
    @DisplayName("존재하지 않는 청구 조회 시 404")
    void get_detail_fail_not_found() throws Exception {
        // given
        given(expenseService.getDetail(any(), eq(99L)))
                .willThrow(new BusinessException(ExpenseErrorCode.CLAIM_NOT_FOUND));

        // when & then
        mockMvc.perform(get("/api/v1/expenses/{id}", 99L))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = LOGIN_ID)
    @DisplayName("영수증 다운로드 성공 — 바이트 + 첨부 헤더")
    void download_receipt_success() throws Exception {
        // given
        byte[] content = "image-bytes".getBytes();
        given(expenseService.downloadReceipt(eq(LOGIN_ID), eq(7L), eq(101L)))
                .willReturn(new ExpenseReceiptDownload("receipt.png", "image/png", content));

        // when & then
        mockMvc.perform(get("/api/v1/expenses/{id}/receipts/{fileId}", 7L, 101L))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"receipt.png\""))
                .andExpect(content().contentType(MediaType.IMAGE_PNG))
                .andExpect(content().bytes(content));
    }

    @Test
    @WithMockUser(username = LOGIN_ID)
    @DisplayName("연결되지 않은 영수증 다운로드 시 404")
    void download_receipt_fail_not_found() throws Exception {
        // given
        given(expenseService.downloadReceipt(eq(LOGIN_ID), eq(7L), eq(999L)))
                .willThrow(new BusinessException(ExpenseErrorCode.CLAIM_NOT_FOUND));

        // when & then
        mockMvc.perform(get("/api/v1/expenses/{id}/receipts/{fileId}", 7L, 999L))
                .andExpect(status().isNotFound());
    }
}
