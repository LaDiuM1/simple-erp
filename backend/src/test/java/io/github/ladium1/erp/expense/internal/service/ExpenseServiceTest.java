package io.github.ladium1.erp.expense.internal.service;

import io.github.ladium1.erp.approval.api.ApprovalApi;
import io.github.ladium1.erp.approval.api.ApprovalDocType;
import io.github.ladium1.erp.approval.api.dto.ApprovalSubmitCommand;
import io.github.ladium1.erp.employee.api.EmployeeApi;
import io.github.ladium1.erp.employee.api.dto.EmployeeInfo;
import io.github.ladium1.erp.expense.internal.dto.ExpenseCreateRequest;
import io.github.ladium1.erp.expense.internal.dto.ExpenseDetailResponse;
import io.github.ladium1.erp.expense.internal.dto.ExpenseReceiptDownload;
import io.github.ladium1.erp.expense.internal.dto.ExpenseSearchCondition;
import io.github.ladium1.erp.expense.internal.dto.ExpenseSearchScope;
import io.github.ladium1.erp.expense.internal.dto.ExpenseSummaryResponse;
import io.github.ladium1.erp.expense.internal.entity.ExpenseCategory;
import io.github.ladium1.erp.expense.internal.entity.ExpenseClaim;
import io.github.ladium1.erp.expense.internal.entity.ExpenseStatus;
import io.github.ladium1.erp.expense.internal.exception.ExpenseErrorCode;
import io.github.ladium1.erp.expense.internal.repository.ExpenseClaimRepository;
import io.github.ladium1.erp.global.exception.BusinessException;
import io.github.ladium1.erp.global.security.MenuPermissionEvaluator;
import io.github.ladium1.erp.global.storage.FileStorageApi;
import io.github.ladium1.erp.global.storage.StoredFileInfo;
import io.github.ladium1.erp.global.web.PageResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ExpenseServiceTest {

    @InjectMocks
    private ExpenseService expenseService;

    @InjectMocks
    private ExpenseApprovalResultHandler expenseApprovalResultHandler;

    @Mock private ExpenseClaimRepository expenseClaimRepository;
    @Mock private ApprovalApi approvalApi;
    @Mock private EmployeeApi employeeApi;
    @Mock private FileStorageApi fileStorageApi;
    @Mock private MenuPermissionEvaluator menuPermissionEvaluator;

    private final String TEST_LOGIN_ID = "testUser";
    private final Long TEST_EMPLOYEE_ID = 1L;

    private EmployeeInfo employeeInfo() {
        return EmployeeInfo.builder()
                .id(TEST_EMPLOYEE_ID)
                .loginId(TEST_LOGIN_ID)
                .name("홍길동")
                .build();
    }

    private ExpenseClaim claim() {
        return ExpenseClaim.builder()
                .claimantId(TEST_EMPLOYEE_ID)
                .title("6월 출장 경비")
                .totalAmount(new BigDecimal("100000"))
                .build();
    }

    @Test
    @DisplayName("create 성공 — 총액 합산 + 상신 + 문서 ID 링크")
    void create_success() {
        // given
        ExpenseCreateRequest request = new ExpenseCreateRequest(
                "6월 출장 경비",
                List.of(
                        new ExpenseCreateRequest.ItemRequest(
                                LocalDate.of(2026, 6, 1), ExpenseCategory.TRANSPORT,
                                new BigDecimal("12000"), "KTX 왕복", 101L),
                        new ExpenseCreateRequest.ItemRequest(
                                LocalDate.of(2026, 6, 1), ExpenseCategory.MEAL,
                                new BigDecimal("8000"), "점심", null),
                        new ExpenseCreateRequest.ItemRequest(
                                LocalDate.of(2026, 6, 1), ExpenseCategory.LODGING,
                                new BigDecimal("80000"), "호텔 1박", 102L)
                ),
                List.of(5L, 6L)
        );
        given(employeeApi.findByLoginId(TEST_LOGIN_ID)).willReturn(Optional.of(employeeInfo()));
        given(expenseClaimRepository.save(any(ExpenseClaim.class))).willAnswer(invocation -> {
            ExpenseClaim saved = invocation.getArgument(0);
            ReflectionTestUtils.setField(saved, "id", 10L);
            return saved;
        });
        given(approvalApi.submit(any(ApprovalSubmitCommand.class))).willReturn(99L);

        // when
        Long claimId = expenseService.create(TEST_LOGIN_ID, request);

        // then
        assertThat(claimId).isEqualTo(10L);

        ArgumentCaptor<ExpenseClaim> claimCaptor = ArgumentCaptor.forClass(ExpenseClaim.class);
        verify(expenseClaimRepository).save(claimCaptor.capture());
        ExpenseClaim claim = claimCaptor.getValue();
        assertThat(claim.getTotalAmount()).isEqualByComparingTo(new BigDecimal("100000"));
        assertThat(claim.getItems()).hasSize(3);
        assertThat(claim.getStatus()).isEqualTo(ExpenseStatus.IN_PROGRESS);
        assertThat(claim.getApprovalDocumentId()).isEqualTo(99L);

        ArgumentCaptor<ApprovalSubmitCommand> commandCaptor = ArgumentCaptor.forClass(ApprovalSubmitCommand.class);
        verify(approvalApi).submit(commandCaptor.capture());
        ApprovalSubmitCommand command = commandCaptor.getValue();
        assertThat(command.docType()).isEqualTo(ApprovalDocType.EXPENSE);
        assertThat(command.title()).isEqualTo("6월 출장 경비");
        assertThat(command.content()).isEqualTo("""
                2026-06-01 교통비 12,000원 KTX 왕복
                2026-06-01 식대 8,000원 점심
                2026-06-01 숙박비 80,000원 호텔 1박
                합계 100,000원""");
        assertThat(command.drafterId()).isEqualTo(TEST_EMPLOYEE_ID);
        assertThat(command.refId()).isEqualTo(10L);
        assertThat(command.approverIds()).containsExactly(5L, 6L);
        assertThat(command.attachmentFileIds()).containsExactly(101L, 102L);
    }

    @Test
    @DisplayName("빈 항목 등록 시 400")
    void create_fail_empty_items() {
        // given
        ExpenseCreateRequest request = new ExpenseCreateRequest("빈 청구", List.of(), List.of(5L));
        given(employeeApi.findByLoginId(TEST_LOGIN_ID)).willReturn(Optional.of(employeeInfo()));

        // when & then
        assertThatThrownBy(() -> expenseService.create(TEST_LOGIN_ID, request))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ExpenseErrorCode.EMPTY_ITEMS);
        verify(expenseClaimRepository, never()).save(any());
        verify(approvalApi, never()).submit(any());
    }

    @Test
    @DisplayName("승인 콜백 — APPROVED 전이")
    void on_approved_transitions_to_approved() {
        // given
        ExpenseClaim claim = claim();
        given(expenseClaimRepository.findById(10L)).willReturn(Optional.of(claim));

        // when
        expenseApprovalResultHandler.onApproved(10L);

        // then
        assertThat(claim.getStatus()).isEqualTo(ExpenseStatus.APPROVED);
    }

    @Test
    @DisplayName("반려 콜백 — REJECTED 전이")
    void on_rejected_transitions_to_rejected() {
        // given
        ExpenseClaim claim = claim();
        given(expenseClaimRepository.findById(10L)).willReturn(Optional.of(claim));

        // when
        expenseApprovalResultHandler.onRejected(10L);

        // then
        assertThat(claim.getStatus()).isEqualTo(ExpenseStatus.REJECTED);
    }

    @Test
    @DisplayName("무관한 사용자 상세 조회 시 404 은닉")
    void get_detail_fail_not_related() {
        // given
        EmployeeInfo other = EmployeeInfo.builder().id(2L).loginId(TEST_LOGIN_ID).name("김철수").build();
        ExpenseClaim claim = claim();
        claim.linkApprovalDocument(99L);
        given(employeeApi.findByLoginId(TEST_LOGIN_ID)).willReturn(Optional.of(other));
        given(expenseClaimRepository.findById(10L)).willReturn(Optional.of(claim));
        given(approvalApi.involves(99L, 2L)).willReturn(false);
        given(menuPermissionEvaluator.canWrite(any(), eq("EXPENSES"))).willReturn(false);

        // when & then
        assertThatThrownBy(() -> expenseService.getDetail(TEST_LOGIN_ID, 10L))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ExpenseErrorCode.CLAIM_NOT_FOUND);
    }

    @Test
    @DisplayName("결재 관련자 상세 조회 성공 — involves 로 접근 허용")
    void get_detail_success_involved_approver() {
        // given
        EmployeeInfo approver = EmployeeInfo.builder().id(2L).loginId(TEST_LOGIN_ID).name("김철수").build();
        ExpenseClaim claim = claim();
        claim.linkApprovalDocument(99L);
        given(employeeApi.findByLoginId(TEST_LOGIN_ID)).willReturn(Optional.of(approver));
        given(expenseClaimRepository.findById(10L)).willReturn(Optional.of(claim));
        given(approvalApi.involves(99L, 2L)).willReturn(true);
        given(employeeApi.getById(TEST_EMPLOYEE_ID)).willReturn(employeeInfo());
        given(fileStorageApi.getInfos(List.of())).willReturn(List.of());

        // when
        ExpenseDetailResponse response = expenseService.getDetail(TEST_LOGIN_ID, 10L);

        // then
        assertThat(response.claimantName()).isEqualTo("홍길동");
    }

    @Test
    @DisplayName("scope ALL — 정산 관리자 아니면 403")
    void search_fail_all_scope_without_write_permission() {
        // given
        ExpenseSearchCondition condition = new ExpenseSearchCondition(null, null, null, null);
        given(menuPermissionEvaluator.canWrite(any(), eq("EXPENSES"))).willReturn(false);

        // when & then
        assertThatThrownBy(() -> expenseService.search(TEST_LOGIN_ID, ExpenseSearchScope.ALL, condition, PageRequest.of(0, 20)))
                .isInstanceOf(AccessDeniedException.class);
        verify(expenseClaimRepository, never()).search(any(), any(), any());
    }

    @Test
    @DisplayName("scope ALL — 정산 관리자는 claimantId 없이 전체 검색 + 청구자명 매핑")
    void search_success_all_scope_for_settlement_manager() {
        // given
        ExpenseSearchCondition condition = new ExpenseSearchCondition(null, null, null, null);
        given(menuPermissionEvaluator.canWrite(any(), eq("EXPENSES"))).willReturn(true);
        given(expenseClaimRepository.search(isNull(), eq(condition), any(Pageable.class)))
                .willReturn(new PageImpl<>(List.of(claim()), PageRequest.of(0, 20), 1));
        given(employeeApi.findByIds(List.of(TEST_EMPLOYEE_ID))).willReturn(List.of(employeeInfo()));

        // when
        PageResponse<ExpenseSummaryResponse> response =
                expenseService.search(TEST_LOGIN_ID, ExpenseSearchScope.ALL, condition, PageRequest.of(0, 20));

        // then
        verify(expenseClaimRepository).search(isNull(), eq(condition), any(Pageable.class));
        assertThat(response.content().getFirst().claimantName()).isEqualTo("홍길동");
    }

    @Test
    @DisplayName("영수증 다운로드 성공 — 청구 항목에 연결된 파일")
    void download_receipt_success() {
        // given
        ExpenseClaim claim = claim();
        claim.addItem(LocalDate.of(2026, 6, 1), ExpenseCategory.TRANSPORT,
                new BigDecimal("12000"), "KTX 왕복", 101L);
        given(employeeApi.findByLoginId(TEST_LOGIN_ID)).willReturn(Optional.of(employeeInfo()));
        given(expenseClaimRepository.findById(10L)).willReturn(Optional.of(claim));
        given(fileStorageApi.getInfo(101L)).willReturn(
                StoredFileInfo.builder().id(101L).originalName("영수증.png").contentType("image/png").build());
        given(fileStorageApi.loadContent(101L)).willReturn("image-bytes".getBytes());

        // when
        ExpenseReceiptDownload download = expenseService.downloadReceipt(TEST_LOGIN_ID, 10L, 101L);

        // then
        assertThat(download.name()).isEqualTo("영수증.png");
        assertThat(download.contentType()).isEqualTo("image/png");
        assertThat(download.content()).isEqualTo("image-bytes".getBytes());
    }

    @Test
    @DisplayName("청구에 연결되지 않은 영수증 다운로드 시 404 은닉")
    void download_receipt_fail_file_not_attached() {
        // given
        ExpenseClaim claim = claim();
        claim.addItem(LocalDate.of(2026, 6, 1), ExpenseCategory.TRANSPORT,
                new BigDecimal("12000"), "KTX 왕복", 101L);
        given(employeeApi.findByLoginId(TEST_LOGIN_ID)).willReturn(Optional.of(employeeInfo()));
        given(expenseClaimRepository.findById(10L)).willReturn(Optional.of(claim));

        // when & then
        assertThatThrownBy(() -> expenseService.downloadReceipt(TEST_LOGIN_ID, 10L, 999L))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ExpenseErrorCode.CLAIM_NOT_FOUND);
        verify(fileStorageApi, never()).loadContent(any());
    }
}
