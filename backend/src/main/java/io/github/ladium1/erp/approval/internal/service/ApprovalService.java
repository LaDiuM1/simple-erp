package io.github.ladium1.erp.approval.internal.service;

import io.github.ladium1.erp.approval.api.ApprovalApi;
import io.github.ladium1.erp.approval.api.ApprovalDocType;
import io.github.ladium1.erp.approval.api.ApprovalResultHandler;
import io.github.ladium1.erp.approval.api.dto.ApprovalSubmitCommand;
import io.github.ladium1.erp.approval.internal.dto.ApprovalAttachmentDownload;
import io.github.ladium1.erp.approval.internal.dto.ApprovalAttachmentResponse;
import io.github.ladium1.erp.approval.internal.dto.ApprovalCreateRequest;
import io.github.ladium1.erp.approval.internal.dto.ApprovalDetailResponse;
import io.github.ladium1.erp.approval.internal.dto.ApprovalSearchCondition;
import io.github.ladium1.erp.approval.internal.dto.ApprovalStepResponse;
import io.github.ladium1.erp.approval.internal.dto.ApprovalSummaryResponse;
import io.github.ladium1.erp.approval.internal.dto.DecisionRequest;
import io.github.ladium1.erp.approval.internal.entity.ApprovalDocument;
import io.github.ladium1.erp.approval.internal.entity.ApprovalStep;
import io.github.ladium1.erp.approval.internal.exception.ApprovalErrorCode;
import io.github.ladium1.erp.approval.internal.mapper.ApprovalMapper;
import io.github.ladium1.erp.approval.internal.repository.ApprovalDocumentRepository;
import io.github.ladium1.erp.employee.api.EmployeeApi;
import io.github.ladium1.erp.employee.api.dto.EmployeeInfo;
import io.github.ladium1.erp.global.audit.AuditAction;
import io.github.ladium1.erp.global.audit.Auditable;
import io.github.ladium1.erp.global.exception.BusinessException;
import io.github.ladium1.erp.global.menu.Menu;
import io.github.ladium1.erp.global.storage.FileStorageApi;
import io.github.ladium1.erp.global.storage.StoredFileInfo;
import io.github.ladium1.erp.global.web.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ApprovalService implements ApprovalApi {

    private final ApprovalDocumentRepository approvalDocumentRepository;
    private final ApprovalMapper approvalMapper;
    private final EmployeeApi employeeApi;
    private final FileStorageApi fileStorageApi;
    private final List<ApprovalResultHandler> resultHandlers;

    @Override
    @Auditable(menu = Menu.APPROVALS, action = AuditAction.CREATE, targetType = "ApprovalDocument", targetIdFromReturn = true)
    @Transactional
    public Long submit(ApprovalSubmitCommand command) {
        validateApprovalLine(command.drafterId(), command.approverIds());

        ApprovalDocument document = ApprovalDocument.builder()
                .docType(command.docType())
                .title(command.title())
                .content(command.content())
                .drafterId(command.drafterId())
                .refId(command.refId())
                .attachmentFileIds(command.attachmentFileIds())
                .build();
        command.approverIds().forEach(document::addStep);

        return approvalDocumentRepository.save(document).getId();
    }

    /**
     * GENERAL 기안 작성 — 기안자 = 현재 사용자. submit 을 자기 호출하므로 감사 기록은 이 메서드가 남긴다.
     */
    @Auditable(menu = Menu.APPROVALS, action = AuditAction.CREATE, targetType = "ApprovalDocument", targetIdFromReturn = true)
    @Transactional
    public Long createGeneral(String loginId, ApprovalCreateRequest request) {
        Long drafterId = currentEmployeeId(loginId);
        return submit(ApprovalSubmitCommand.builder()
                .docType(ApprovalDocType.GENERAL)
                .title(request.title())
                .content(request.content())
                .drafterId(drafterId)
                .approverIds(request.approverIds())
                .attachmentFileIds(request.attachmentFileIds())
                .build());
    }

    public PageResponse<ApprovalSummaryResponse> search(String loginId, ApprovalSearchCondition condition, Pageable pageable) {
        Long employeeId = currentEmployeeId(loginId);
        Page<ApprovalDocument> page = approvalDocumentRepository.search(employeeId, condition, pageable);

        Map<Long, String> nameById = employeeNames(
                page.getContent().stream().map(ApprovalDocument::getDrafterId).distinct().toList()
        );
        return PageResponse.of(page.map(document ->
                approvalMapper.toSummaryResponse(document, nameById.get(document.getDrafterId()), document.getSteps().size())
        ));
    }

    /**
     * 상세 조회 — 관련자 (기안자 또는 결재선 포함) 가 아니면 존재 은닉을 위해 NOT_FOUND.
     */
    public ApprovalDetailResponse getDetail(String loginId, Long documentId) {
        Long employeeId = currentEmployeeId(loginId);
        ApprovalDocument document = getDocument(documentId);
        if (!document.involves(employeeId)) {
            throw new BusinessException(ApprovalErrorCode.DOCUMENT_NOT_FOUND);
        }

        Map<Long, String> nameById = employeeNames(
                Stream.concat(
                        Stream.of(document.getDrafterId()),
                        document.getSteps().stream().map(ApprovalStep::getApproverId)
                ).distinct().toList()
        );
        List<ApprovalStepResponse> steps = document.getSteps().stream()
                .map(step -> approvalMapper.toStepResponse(step, nameById.get(step.getApproverId())))
                .toList();
        List<ApprovalAttachmentResponse> attachments = fileStorageApi.getInfos(document.getAttachmentFileIds()).stream()
                .map(approvalMapper::toAttachmentResponse)
                .toList();

        boolean myTurn = !document.isCompleted() && document.currentStep().getApproverId().equals(employeeId);
        boolean cancelable = document.getDrafterId().equals(employeeId)
                && !document.isCompleted()
                && document.isAllStepsPending();

        return approvalMapper.toDetailResponse(
                document, nameById.get(document.getDrafterId()), steps, attachments, myTurn, cancelable);
    }

    @Override
    public boolean involves(Long documentId, Long employeeId) {
        return approvalDocumentRepository.findById(documentId)
                .map(document -> document.involves(employeeId))
                .orElse(false);
    }

    /**
     * 첨부 파일 다운로드 — 관련자가 아니거나 문서의 첨부가 아니면 존재 은닉을 위해 NOT_FOUND.
     */
    public ApprovalAttachmentDownload downloadAttachment(String loginId, Long documentId, Long fileId) {
        Long employeeId = currentEmployeeId(loginId);
        ApprovalDocument document = getDocument(documentId);
        if (!document.involves(employeeId) || !document.getAttachmentFileIds().contains(fileId)) {
            throw new BusinessException(ApprovalErrorCode.DOCUMENT_NOT_FOUND);
        }

        StoredFileInfo info = fileStorageApi.getInfo(fileId);
        return new ApprovalAttachmentDownload(info.originalName(), info.contentType(), fileStorageApi.loadContent(fileId));
    }

    @Auditable(menu = Menu.APPROVALS, action = AuditAction.UPDATE, targetType = "ApprovalDocument", targetIdParam = "documentId")
    @Transactional
    public void approve(String loginId, Long documentId, DecisionRequest request) {
        ApprovalDocument document = getDocument(documentId);
        ApprovalStep step = decidableStep(document, currentEmployeeId(loginId));

        step.approve(request.comment());
        if (document.isLastStep()) {
            document.markApproved();
            notifyApproved(document);
        } else {
            document.advanceStep();
        }
    }

    @Auditable(menu = Menu.APPROVALS, action = AuditAction.UPDATE, targetType = "ApprovalDocument", targetIdParam = "documentId")
    @Transactional
    public void reject(String loginId, Long documentId, DecisionRequest request) {
        ApprovalDocument document = getDocument(documentId);
        ApprovalStep step = decidableStep(document, currentEmployeeId(loginId));

        step.reject(request.comment());
        document.markRejected();
        notifyRejected(document);
    }

    /**
     * 상신 취소 — 기안자 본인 + 아직 아무 단계도 결정되지 않은 문서만.
     */
    @Auditable(menu = Menu.APPROVALS, action = AuditAction.UPDATE, targetType = "ApprovalDocument", targetIdParam = "documentId")
    @Transactional
    public void cancel(String loginId, Long documentId) {
        Long employeeId = currentEmployeeId(loginId);
        ApprovalDocument document = getDocument(documentId);
        if (document.isCompleted()) {
            throw new BusinessException(ApprovalErrorCode.ALREADY_COMPLETED);
        }
        if (!document.getDrafterId().equals(employeeId) || !document.isAllStepsPending()) {
            throw new BusinessException(ApprovalErrorCode.CANCEL_NOT_ALLOWED);
        }

        document.markCanceled();
        notifyRejected(document);
    }

    private void validateApprovalLine(Long drafterId, List<Long> approverIds) {
        if (approverIds == null || approverIds.isEmpty()) {
            throw new BusinessException(ApprovalErrorCode.INVALID_APPROVAL_LINE);
        }
        if (new HashSet<>(approverIds).size() != approverIds.size()) {
            throw new BusinessException(ApprovalErrorCode.INVALID_APPROVAL_LINE);
        }
        if (approverIds.contains(drafterId)) {
            throw new BusinessException(ApprovalErrorCode.INVALID_APPROVAL_LINE);
        }
        // 존재하지 않는 직원 거부 — EmployeeInfo 에 재직 상태가 없어 존재 검증까지만
        if (employeeApi.findByIds(approverIds).size() != approverIds.size()) {
            throw new BusinessException(ApprovalErrorCode.INVALID_APPROVAL_LINE);
        }
    }

    /**
     * 현재 차례의 결재 단계 반환 — 완료 문서 / 차례 아닌 결재자 거부.
     */
    private ApprovalStep decidableStep(ApprovalDocument document, Long employeeId) {
        if (document.isCompleted()) {
            throw new BusinessException(ApprovalErrorCode.ALREADY_COMPLETED);
        }
        ApprovalStep step = document.currentStep();
        if (!step.getApproverId().equals(employeeId)) {
            throw new BusinessException(ApprovalErrorCode.NOT_YOUR_TURN);
        }
        return step;
    }

    private void notifyApproved(ApprovalDocument document) {
        if (document.getRefId() == null) {
            return;
        }
        resultHandlers.stream()
                .filter(handler -> handler.docType() == document.getDocType())
                .forEach(handler -> handler.onApproved(document.getRefId()));
    }

    private void notifyRejected(ApprovalDocument document) {
        if (document.getRefId() == null) {
            return;
        }
        resultHandlers.stream()
                .filter(handler -> handler.docType() == document.getDocType())
                .forEach(handler -> handler.onRejected(document.getRefId()));
    }

    private ApprovalDocument getDocument(Long documentId) {
        return approvalDocumentRepository.findById(documentId)
                .orElseThrow(() -> new BusinessException(ApprovalErrorCode.DOCUMENT_NOT_FOUND));
    }

    private Long currentEmployeeId(String loginId) {
        return employeeApi.findByLoginId(loginId)
                .map(EmployeeInfo::id)
                .orElseThrow(() -> new AccessDeniedException("직원 정보를 확인할 수 없습니다."));
    }

    private Map<Long, String> employeeNames(List<Long> employeeIds) {
        return employeeApi.findByIds(employeeIds).stream()
                .collect(Collectors.toMap(EmployeeInfo::id, EmployeeInfo::name));
    }
}
