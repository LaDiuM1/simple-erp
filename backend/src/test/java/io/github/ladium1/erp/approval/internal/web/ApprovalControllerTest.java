package io.github.ladium1.erp.approval.internal.web;

import io.github.ladium1.erp.approval.api.ApprovalDocType;
import io.github.ladium1.erp.approval.internal.dto.ApprovalAttachmentDownload;
import io.github.ladium1.erp.approval.internal.dto.ApprovalCreateRequest;
import io.github.ladium1.erp.approval.internal.dto.ApprovalDetailResponse;
import io.github.ladium1.erp.approval.internal.dto.ApprovalSummaryResponse;
import io.github.ladium1.erp.approval.internal.dto.DecisionRequest;
import io.github.ladium1.erp.approval.internal.entity.ApprovalStatus;
import io.github.ladium1.erp.approval.internal.exception.ApprovalErrorCode;
import io.github.ladium1.erp.approval.internal.service.ApprovalService;
import io.github.ladium1.erp.global.exception.BusinessException;
import io.github.ladium1.erp.global.security.MenuPermissionEvaluator;
import io.github.ladium1.erp.global.validation.RequestTextPolicy;
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

import java.util.Arrays;
import java.util.List;
import java.util.stream.LongStream;

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

@WebMvcTest(ApprovalController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(ApprovalControllerTest.TestWebMvcConfig.class)
class ApprovalControllerTest {

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
    private ApprovalService approvalService;

    @MockitoBean
    private MenuPermissionEvaluator menuPermissionEvaluator;

    @BeforeEach
    void allow_all_permissions() {
        given(menuPermissionEvaluator.canRead(any(), any())).willReturn(true);
        given(menuPermissionEvaluator.canWrite(any(), any())).willReturn(true);
    }

    @Test
    @WithMockUser(username = LOGIN_ID)
    @DisplayName("기안 등록 성공")
    void create_success() throws Exception {
        // given
        ApprovalCreateRequest request = new ApprovalCreateRequest(
                "비품 구매 기안", "본문", List.of(2L, 3L), List.of(101L)
        );
        given(approvalService.createGeneral(any(), any())).willReturn(42L);

        // when & then
        mockMvc.perform(post("/api/v1/approvals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(42));
        verify(approvalService).createGeneral(eq(LOGIN_ID), any());
    }

    @Test
    @WithMockUser(username = LOGIN_ID)
    @DisplayName("한글 본문은 4,000자까지 기안 허용")
    void create_accepts_max_korean_content() throws Exception {
        // given
        ApprovalCreateRequest request = new ApprovalCreateRequest(
                "비품 구매 기안", "가".repeat(RequestTextPolicy.MAX_LONG_TEXT_LENGTH), List.of(2L), null
        );
        given(approvalService.createGeneral(any(), any())).willReturn(43L);

        // when & then
        mockMvc.perform(post("/api/v1/approvals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(43));
        verify(approvalService).createGeneral(eq(LOGIN_ID), any());
    }

    @Test
    @WithMockUser(username = LOGIN_ID)
    @DisplayName("한글 본문이 4,000자를 넘으면 기안 서비스 호출 전에 400")
    void create_rejects_korean_content_over_limit() throws Exception {
        ApprovalCreateRequest request = new ApprovalCreateRequest(
                "비품 구매 기안", "가".repeat(RequestTextPolicy.MAX_LONG_TEXT_LENGTH + 1), List.of(2L), null
        );

        mockMvc.perform(post("/api/v1/approvals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
        verify(approvalService, never()).createGeneral(any(), any());
    }

    @Test
    @WithMockUser(username = LOGIN_ID)
    @DisplayName("결재선은 요청 상한인 20명까지 허용")
    void create_accepts_max_approver_ids() throws Exception {
        ApprovalCreateRequest request = new ApprovalCreateRequest(
                "월간 구매 기안", "본문", LongStream.rangeClosed(2, 21).boxed().toList(), null
        );
        given(approvalService.createGeneral(any(), any())).willReturn(43L);

        mockMvc.perform(post("/api/v1/approvals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(43));
        verify(approvalService).createGeneral(eq(LOGIN_ID), any());
    }

    @Test
    @WithMockUser(username = LOGIN_ID)
    @DisplayName("결재선이 20명을 넘으면 서비스 호출 전에 400")
    void create_rejects_approver_ids_over_limit() throws Exception {
        ApprovalCreateRequest request = new ApprovalCreateRequest(
                "월간 구매 기안", "본문", LongStream.rangeClosed(2, 22).boxed().toList(), null
        );

        mockMvc.perform(post("/api/v1/approvals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
        verify(approvalService, never()).createGeneral(any(), any());
    }

    @Test
    @WithMockUser(username = LOGIN_ID)
    @DisplayName("결재선에 null 직원이 있으면 서비스 호출 전에 400")
    void create_rejects_null_approver_id() throws Exception {
        ApprovalCreateRequest request = new ApprovalCreateRequest(
                "월간 구매 기안", "본문", Arrays.asList(2L, null), null
        );

        mockMvc.perform(post("/api/v1/approvals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
        verify(approvalService, never()).createGeneral(any(), any());
    }

    @Test
    @WithMockUser(username = LOGIN_ID)
    @DisplayName("첨부가 20개를 넘으면 서비스 호출 전에 400")
    void create_rejects_attachments_over_limit() throws Exception {
        ApprovalCreateRequest request = new ApprovalCreateRequest(
                "월간 구매 기안", "본문", List.of(2L), LongStream.rangeClosed(1, 21).boxed().toList());

        mockMvc.perform(post("/api/v1/approvals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
        verify(approvalService, never()).createGeneral(any(), any());
    }

    @Test
    @WithMockUser(username = LOGIN_ID)
    @DisplayName("첨부에 null이 있으면 서비스 호출 전에 400")
    void create_rejects_null_attachment() throws Exception {
        ApprovalCreateRequest request = new ApprovalCreateRequest(
                "월간 구매 기안", "본문", List.of(2L), Arrays.asList(1L, null));

        mockMvc.perform(post("/api/v1/approvals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
        verify(approvalService, never()).createGeneral(any(), any());
    }

    @Test
    @WithMockUser(username = LOGIN_ID)
    @DisplayName("중복 첨부는 서비스 호출 전에 400")
    void create_rejects_duplicate_attachments() throws Exception {
        ApprovalCreateRequest request = new ApprovalCreateRequest(
                "월간 구매 기안", "본문", List.of(2L), List.of(1L, 1L));

        mockMvc.perform(post("/api/v1/approvals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
        verify(approvalService, never()).createGeneral(any(), any());
    }

    @Test
    @WithMockUser(username = LOGIN_ID)
    @DisplayName("빈 결재선 등록 시 400")
    void create_fail_empty_approver_ids() throws Exception {
        // given
        ApprovalCreateRequest request = new ApprovalCreateRequest(
                "비품 구매 기안", "본문", List.of(), null
        );

        // when & then
        mockMvc.perform(post("/api/v1/approvals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
        verify(approvalService, never()).createGeneral(any(), any());
    }

    @Test
    @WithMockUser(username = LOGIN_ID)
    @DisplayName("결재함 목록 조회 성공")
    void search_success() throws Exception {
        // given
        ApprovalSummaryResponse summary = ApprovalSummaryResponse.builder()
                .id(1L).docType(ApprovalDocType.GENERAL)
                .title("비품 구매 기안").drafterName("홍길동")
                .status(ApprovalStatus.IN_PROGRESS)
                .currentStepOrder(1).totalSteps(2)
                .build();
        PageResponse<ApprovalSummaryResponse> page = new PageResponse<>(
                List.of(summary), 0, 20, 1, 1, false
        );
        given(approvalService.search(any(), any(), any())).willReturn(page);

        // when & then
        mockMvc.perform(get("/api/v1/approvals").param("box", "DRAFTED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].title").value("비품 구매 기안"))
                .andExpect(jsonPath("$.data.content[0].totalSteps").value(2))
                .andExpect(jsonPath("$.data.totalElements").value(1));
    }

    @Test
    @WithMockUser(username = LOGIN_ID)
    @DisplayName("결재 문서 상세 조회 성공")
    void get_detail_success() throws Exception {
        // given
        ApprovalDetailResponse detail = ApprovalDetailResponse.builder()
                .id(7L).docType(ApprovalDocType.GENERAL).title("비품 구매 기안")
                .drafterId(1L).drafterName("홍길동")
                .status(ApprovalStatus.IN_PROGRESS).currentStepOrder(1)
                .steps(List.of()).attachments(List.of())
                .myTurn(true).cancelable(false)
                .build();
        given(approvalService.getDetail(any(), eq(7L))).willReturn(detail);

        // when & then
        mockMvc.perform(get("/api/v1/approvals/{id}", 7L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.title").value("비품 구매 기안"))
                .andExpect(jsonPath("$.data.myTurn").value(true));
    }

    @Test
    @WithMockUser(username = LOGIN_ID)
    @DisplayName("존재하지 않는 문서 조회 시 404")
    void get_detail_fail_not_found() throws Exception {
        // given
        given(approvalService.getDetail(any(), eq(99L)))
                .willThrow(new BusinessException(ApprovalErrorCode.DOCUMENT_NOT_FOUND));

        // when & then
        mockMvc.perform(get("/api/v1/approvals/{id}", 99L))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = LOGIN_ID)
    @DisplayName("첨부 다운로드 성공 — 바이트 + 첨부 헤더")
    void download_attachment_success() throws Exception {
        // given
        byte[] bytes = "pdf-bytes".getBytes();
        given(approvalService.downloadAttachment(LOGIN_ID, 7L, 101L))
                .willReturn(new ApprovalAttachmentDownload("receipt.pdf", "application/pdf", bytes));

        // when & then
        mockMvc.perform(get("/api/v1/approvals/{id}/attachments/{fileId}", 7L, 101L))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"receipt.pdf\""))
                .andExpect(content().contentType(MediaType.APPLICATION_PDF))
                .andExpect(content().bytes(bytes));
    }

    @Test
    @WithMockUser(username = LOGIN_ID)
    @DisplayName("파싱 불가 contentType — octet-stream 폴백")
    void download_attachment_falls_back_to_octet_stream() throws Exception {
        // given
        byte[] bytes = "raw-bytes".getBytes();
        given(approvalService.downloadAttachment(LOGIN_ID, 7L, 101L))
                .willReturn(new ApprovalAttachmentDownload("raw.bin", "invalid-content-type", bytes));

        // when & then
        mockMvc.perform(get("/api/v1/approvals/{id}/attachments/{fileId}", 7L, 101L))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_OCTET_STREAM))
                .andExpect(content().bytes(bytes));
    }

    @Test
    @WithMockUser(username = LOGIN_ID)
    @DisplayName("문서 첨부가 아닌 파일 다운로드 시 404")
    void download_attachment_fail_not_found() throws Exception {
        // given
        given(approvalService.downloadAttachment(LOGIN_ID, 7L, 999L))
                .willThrow(new BusinessException(ApprovalErrorCode.DOCUMENT_NOT_FOUND));

        // when & then
        mockMvc.perform(get("/api/v1/approvals/{id}/attachments/{fileId}", 7L, 999L))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = LOGIN_ID)
    @DisplayName("승인 성공")
    void approve_success() throws Exception {
        // when & then
        mockMvc.perform(post("/api/v1/approvals/{id}/approve", 7L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new DecisionRequest("확인했습니다"))))
                .andExpect(status().isNoContent());
        verify(approvalService).approve(LOGIN_ID, 7L, new DecisionRequest("확인했습니다"));
    }

    @Test
    @WithMockUser(username = LOGIN_ID)
    @DisplayName("본문 없는 승인 — 빈 의견으로 위임")
    void approve_success_without_body() throws Exception {
        // when & then
        mockMvc.perform(post("/api/v1/approvals/{id}/approve", 7L))
                .andExpect(status().isNoContent());
        verify(approvalService).approve(LOGIN_ID, 7L, new DecisionRequest(null));
    }

    @Test
    @WithMockUser(username = LOGIN_ID)
    @DisplayName("차례 아닌 승인 시 403")
    void approve_fail_not_your_turn() throws Exception {
        // given
        willThrow(new BusinessException(ApprovalErrorCode.NOT_YOUR_TURN))
                .given(approvalService).approve(eq(LOGIN_ID), eq(7L), any());

        // when & then
        mockMvc.perform(post("/api/v1/approvals/{id}/approve", 7L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new DecisionRequest(null))))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = LOGIN_ID)
    @DisplayName("반려 성공")
    void reject_success() throws Exception {
        // when & then
        mockMvc.perform(post("/api/v1/approvals/{id}/reject", 7L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new DecisionRequest("반려합니다"))))
                .andExpect(status().isNoContent());
        verify(approvalService).reject(LOGIN_ID, 7L, new DecisionRequest("반려합니다"));
    }

    @Test
    @WithMockUser(username = LOGIN_ID)
    @DisplayName("상신 취소 성공")
    void cancel_success() throws Exception {
        // when & then
        mockMvc.perform(post("/api/v1/approvals/{id}/cancel", 7L))
                .andExpect(status().isNoContent());
        verify(approvalService).cancel(LOGIN_ID, 7L);
    }

    @Test
    @WithMockUser(username = LOGIN_ID)
    @DisplayName("결재 시작된 문서 취소 시 409")
    void cancel_fail_not_allowed() throws Exception {
        // given
        willThrow(new BusinessException(ApprovalErrorCode.CANCEL_NOT_ALLOWED))
                .given(approvalService).cancel(LOGIN_ID, 7L);

        // when & then
        mockMvc.perform(post("/api/v1/approvals/{id}/cancel", 7L))
                .andExpect(status().isConflict());
    }
}
