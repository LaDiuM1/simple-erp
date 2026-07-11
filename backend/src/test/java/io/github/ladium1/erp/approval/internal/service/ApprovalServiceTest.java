package io.github.ladium1.erp.approval.internal.service;

import io.github.ladium1.erp.approval.api.ApprovalDocType;
import io.github.ladium1.erp.approval.api.ApprovalResultHandler;
import io.github.ladium1.erp.approval.api.dto.ApprovalSubmitCommand;
import io.github.ladium1.erp.approval.internal.dto.ApprovalAttachmentDownload;
import io.github.ladium1.erp.approval.internal.dto.DecisionRequest;
import io.github.ladium1.erp.approval.internal.entity.ApprovalDocument;
import io.github.ladium1.erp.approval.internal.entity.ApprovalStatus;
import io.github.ladium1.erp.approval.internal.entity.ApprovalStep;
import io.github.ladium1.erp.approval.internal.entity.StepStatus;
import io.github.ladium1.erp.approval.internal.exception.ApprovalErrorCode;
import io.github.ladium1.erp.approval.internal.mapper.ApprovalMapper;
import io.github.ladium1.erp.approval.internal.repository.ApprovalDocumentRepository;
import io.github.ladium1.erp.employee.api.EmployeeApi;
import io.github.ladium1.erp.employee.api.dto.EmployeeInfo;
import io.github.ladium1.erp.global.exception.BusinessException;
import io.github.ladium1.erp.global.storage.FileStorageApi;
import io.github.ladium1.erp.global.storage.StoredFileInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ApprovalServiceTest {

    private ApprovalService approvalService;

    @Mock private ApprovalDocumentRepository approvalDocumentRepository;
    @Mock private ApprovalMapper approvalMapper;
    @Mock private EmployeeApi employeeApi;
    @Mock private FileStorageApi fileStorageApi;
    @Mock private ApprovalResultHandler expenseResultHandler;

    private static final String LOGIN_ID = "testUser";
    private static final Long DRAFTER_ID = 1L;
    private static final Long DOCUMENT_ID = 100L;
    private static final Long REF_ID = 10L;

    @BeforeEach
    void setUp() {
        // List<ApprovalResultHandler> 주입은 @InjectMocks 가 못 다뤄 직접 생성
        approvalService = new ApprovalService(
                approvalDocumentRepository, approvalMapper, employeeApi, fileStorageApi,
                List.of(expenseResultHandler));
    }

    private ApprovalSubmitCommand command(List<Long> approverIds) {
        return ApprovalSubmitCommand.builder()
                .docType(ApprovalDocType.GENERAL)
                .title("테스트 기안")
                .content("본문")
                .drafterId(DRAFTER_ID)
                .approverIds(approverIds)
                .build();
    }

    private ApprovalDocument expenseDocument(List<Long> approverIds) {
        ApprovalDocument document = ApprovalDocument.builder()
                .docType(ApprovalDocType.EXPENSE)
                .title("경비 청구")
                .drafterId(DRAFTER_ID)
                .refId(REF_ID)
                .build();
        approverIds.forEach(document::addStep);
        return document;
    }

    private ApprovalDocument documentWithAttachment(Long fileId) {
        ApprovalDocument document = ApprovalDocument.builder()
                .docType(ApprovalDocType.GENERAL)
                .title("첨부 기안")
                .drafterId(DRAFTER_ID)
                .attachmentFileIds(List.of(fileId))
                .build();
        document.addStep(2L);
        return document;
    }

    private void loginAs(Long employeeId) {
        EmployeeInfo employee = EmployeeInfo.builder().id(employeeId).loginId(LOGIN_ID).name("직원" + employeeId).build();
        given(employeeApi.findByLoginId(LOGIN_ID)).willReturn(Optional.of(employee));
    }

    private void approversExist(List<Long> approverIds) {
        given(employeeApi.findByIds(approverIds)).willReturn(
                approverIds.stream()
                        .map(id -> EmployeeInfo.builder().id(id).name("직원" + id).build())
                        .toList());
    }

    @Test
    @DisplayName("상신 성공 — 결재선 순서대로 step 생성")
    void submit_success() {
        // given
        approversExist(List.of(2L, 3L));
        given(approvalDocumentRepository.save(any(ApprovalDocument.class))).willAnswer(inv -> inv.getArgument(0));

        // when
        approvalService.submit(command(List.of(2L, 3L)));

        // then
        ArgumentCaptor<ApprovalDocument> captor = ArgumentCaptor.forClass(ApprovalDocument.class);
        verify(approvalDocumentRepository).save(captor.capture());
        ApprovalDocument saved = captor.getValue();

        assertThat(saved.getStatus()).isEqualTo(ApprovalStatus.IN_PROGRESS);
        assertThat(saved.getCurrentStepOrder()).isEqualTo(1);
        assertThat(saved.getSteps()).extracting(ApprovalStep::getStepOrder).containsExactly(1, 2);
        assertThat(saved.getSteps()).extracting(ApprovalStep::getApproverId).containsExactly(2L, 3L);
        assertThat(saved.getSteps()).extracting(ApprovalStep::getStatus).containsOnly(StepStatus.PENDING);
    }

    @Test
    @DisplayName("빈 결재선 거부")
    void submit_fail_empty_approval_line() {
        // when & then
        assertThatThrownBy(() -> approvalService.submit(command(List.of())))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ApprovalErrorCode.INVALID_APPROVAL_LINE);
    }

    @Test
    @DisplayName("기안자 포함 결재선 거부")
    void submit_fail_drafter_in_approval_line() {
        // when & then
        assertThatThrownBy(() -> approvalService.submit(command(List.of(2L, DRAFTER_ID))))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ApprovalErrorCode.INVALID_APPROVAL_LINE);
    }

    @Test
    @DisplayName("존재하지 않는 결재자 거부")
    void submit_fail_unknown_approver() {
        // given
        given(employeeApi.findByIds(List.of(2L, 3L))).willReturn(
                List.of(EmployeeInfo.builder().id(2L).name("직원2").build()));

        // when & then
        assertThatThrownBy(() -> approvalService.submit(command(List.of(2L, 3L))))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ApprovalErrorCode.INVALID_APPROVAL_LINE);
    }

    @Test
    @DisplayName("중간 단계 승인 — currentStepOrder 증가")
    void approve_success_middle_step() {
        // given
        ApprovalDocument document = expenseDocument(List.of(2L, 3L));
        loginAs(2L);
        given(approvalDocumentRepository.findById(DOCUMENT_ID)).willReturn(Optional.of(document));

        // when
        approvalService.approve(LOGIN_ID, DOCUMENT_ID, new DecisionRequest("확인했습니다"));

        // then
        assertThat(document.getStatus()).isEqualTo(ApprovalStatus.IN_PROGRESS);
        assertThat(document.getCurrentStepOrder()).isEqualTo(2);
        ApprovalStep decided = document.getSteps().get(0);
        assertThat(decided.getStatus()).isEqualTo(StepStatus.APPROVED);
        assertThat(decided.getComment()).isEqualTo("확인했습니다");
        assertThat(decided.getDecidedAt()).isNotNull();
        verify(expenseResultHandler, never()).onApproved(any());
    }

    @Test
    @DisplayName("마지막 단계 승인 — 문서 APPROVED + onApproved 통지")
    void approve_success_last_step() {
        // given
        ApprovalDocument document = expenseDocument(List.of(2L));
        loginAs(2L);
        given(approvalDocumentRepository.findById(DOCUMENT_ID)).willReturn(Optional.of(document));
        given(expenseResultHandler.docType()).willReturn(ApprovalDocType.EXPENSE);

        // when
        approvalService.approve(LOGIN_ID, DOCUMENT_ID, new DecisionRequest(null));

        // then
        assertThat(document.getStatus()).isEqualTo(ApprovalStatus.APPROVED);
        verify(expenseResultHandler).onApproved(REF_ID);
    }

    @Test
    @DisplayName("차례 아닌 결재자 — NOT_YOUR_TURN")
    void approve_fail_not_your_turn() {
        // given
        ApprovalDocument document = expenseDocument(List.of(2L, 3L));
        loginAs(3L);
        given(approvalDocumentRepository.findById(DOCUMENT_ID)).willReturn(Optional.of(document));

        // when & then
        assertThatThrownBy(() -> approvalService.approve(LOGIN_ID, DOCUMENT_ID, new DecisionRequest(null)))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ApprovalErrorCode.NOT_YOUR_TURN);
    }

    @Test
    @DisplayName("결재선에 없는 사람 — NOT_YOUR_TURN")
    void approve_fail_not_in_approval_line() {
        // given
        ApprovalDocument document = expenseDocument(List.of(2L, 3L));
        loginAs(99L);
        given(approvalDocumentRepository.findById(DOCUMENT_ID)).willReturn(Optional.of(document));

        // when & then
        assertThatThrownBy(() -> approvalService.approve(LOGIN_ID, DOCUMENT_ID, new DecisionRequest(null)))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ApprovalErrorCode.NOT_YOUR_TURN);
    }

    @Test
    @DisplayName("완료 문서 승인 시 409")
    void approve_fail_already_completed() {
        // given
        ApprovalDocument document = expenseDocument(List.of(2L));
        document.markRejected();
        loginAs(2L);
        given(approvalDocumentRepository.findById(DOCUMENT_ID)).willReturn(Optional.of(document));

        // when & then
        assertThatThrownBy(() -> approvalService.approve(LOGIN_ID, DOCUMENT_ID, new DecisionRequest(null)))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ApprovalErrorCode.ALREADY_COMPLETED);
    }

    @Test
    @DisplayName("반려 — 문서 REJECTED + onRejected 통지")
    void reject_success() {
        // given
        ApprovalDocument document = expenseDocument(List.of(2L, 3L));
        loginAs(2L);
        given(approvalDocumentRepository.findById(DOCUMENT_ID)).willReturn(Optional.of(document));
        given(expenseResultHandler.docType()).willReturn(ApprovalDocType.EXPENSE);

        // when
        approvalService.reject(LOGIN_ID, DOCUMENT_ID, new DecisionRequest("반려합니다"));

        // then
        assertThat(document.getStatus()).isEqualTo(ApprovalStatus.REJECTED);
        assertThat(document.getSteps().get(0).getStatus()).isEqualTo(StepStatus.REJECTED);
        verify(expenseResultHandler).onRejected(REF_ID);
    }

    @Test
    @DisplayName("상신 취소 성공 — 문서 CANCELED + onRejected 통지")
    void cancel_success() {
        // given
        ApprovalDocument document = expenseDocument(List.of(2L, 3L));
        loginAs(DRAFTER_ID);
        given(approvalDocumentRepository.findById(DOCUMENT_ID)).willReturn(Optional.of(document));
        given(expenseResultHandler.docType()).willReturn(ApprovalDocType.EXPENSE);

        // when
        approvalService.cancel(LOGIN_ID, DOCUMENT_ID);

        // then
        assertThat(document.getStatus()).isEqualTo(ApprovalStatus.CANCELED);
        verify(expenseResultHandler).onRejected(REF_ID);
    }

    @Test
    @DisplayName("결재 시작 후 취소 — CANCEL_NOT_ALLOWED")
    void cancel_fail_already_started() {
        // given
        ApprovalDocument document = expenseDocument(List.of(2L, 3L));
        document.currentStep().approve("확인");
        document.advanceStep();
        loginAs(DRAFTER_ID);
        given(approvalDocumentRepository.findById(DOCUMENT_ID)).willReturn(Optional.of(document));

        // when & then
        assertThatThrownBy(() -> approvalService.cancel(LOGIN_ID, DOCUMENT_ID))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ApprovalErrorCode.CANCEL_NOT_ALLOWED);
    }

    @Test
    @DisplayName("관련자 아닌 상세 조회 시 404 은닉")
    void get_detail_fail_not_involved() {
        // given
        ApprovalDocument document = expenseDocument(List.of(2L, 3L));
        loginAs(99L);
        given(approvalDocumentRepository.findById(DOCUMENT_ID)).willReturn(Optional.of(document));

        // when & then
        assertThatThrownBy(() -> approvalService.getDetail(LOGIN_ID, DOCUMENT_ID))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ApprovalErrorCode.DOCUMENT_NOT_FOUND);
    }

    @Test
    @DisplayName("involves — 결재선 포함 직원은 true")
    void involves_true_for_approver() {
        given(approvalDocumentRepository.findById(DOCUMENT_ID)).willReturn(Optional.of(expenseDocument(List.of(2L, 3L))));
        assertThat(approvalService.involves(DOCUMENT_ID, 3L)).isTrue();
    }

    @Test
    @DisplayName("involves — 무관한 직원은 false")
    void involves_false_for_unrelated() {
        given(approvalDocumentRepository.findById(DOCUMENT_ID)).willReturn(Optional.of(expenseDocument(List.of(2L, 3L))));
        assertThat(approvalService.involves(DOCUMENT_ID, 99L)).isFalse();
    }

    @Test
    @DisplayName("involves — 문서 없음은 false")
    void involves_false_when_document_missing() {
        given(approvalDocumentRepository.findById(DOCUMENT_ID)).willReturn(Optional.empty());
        assertThat(approvalService.involves(DOCUMENT_ID, 2L)).isFalse();
    }

    @Test
    @DisplayName("첨부 다운로드 성공 — 이름 / contentType / 본체 반환")
    void download_attachment_success() {
        // given
        Long fileId = 200L;
        loginAs(2L);
        given(approvalDocumentRepository.findById(DOCUMENT_ID)).willReturn(Optional.of(documentWithAttachment(fileId)));
        given(fileStorageApi.getInfo(fileId)).willReturn(StoredFileInfo.builder()
                .id(fileId).originalName("영수증.pdf").contentType("application/pdf").size(3L).build());
        given(fileStorageApi.loadContent(fileId)).willReturn(new byte[]{1, 2, 3});

        // when
        ApprovalAttachmentDownload download = approvalService.downloadAttachment(LOGIN_ID, DOCUMENT_ID, fileId);

        // then
        assertThat(download.name()).isEqualTo("영수증.pdf");
        assertThat(download.contentType()).isEqualTo("application/pdf");
        assertThat(download.content()).containsExactly(1, 2, 3);
    }

    @Test
    @DisplayName("관련자 아닌 첨부 다운로드 시 404 은닉")
    void download_attachment_fail_not_involved() {
        // given
        Long fileId = 200L;
        loginAs(99L);
        given(approvalDocumentRepository.findById(DOCUMENT_ID)).willReturn(Optional.of(documentWithAttachment(fileId)));

        // when & then
        assertThatThrownBy(() -> approvalService.downloadAttachment(LOGIN_ID, DOCUMENT_ID, fileId))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ApprovalErrorCode.DOCUMENT_NOT_FOUND);
        verify(fileStorageApi, never()).loadContent(any());
    }

    @Test
    @DisplayName("문서 첨부가 아닌 파일 다운로드 시 404 은닉")
    void download_attachment_fail_not_in_document() {
        // given
        loginAs(2L);
        given(approvalDocumentRepository.findById(DOCUMENT_ID)).willReturn(Optional.of(documentWithAttachment(200L)));

        // when & then
        assertThatThrownBy(() -> approvalService.downloadAttachment(LOGIN_ID, DOCUMENT_ID, 999L))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ApprovalErrorCode.DOCUMENT_NOT_FOUND);
        verify(fileStorageApi, never()).loadContent(any());
    }
}
