package io.github.ladium1.erp.approval.internal.entity;

import io.github.ladium1.erp.approval.api.ApprovalDocType;
import io.github.ladium1.erp.global.jpa.BaseEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * 결재 문서 — 내용을 모르는 generic 상태머신의 루트.
 * <p>
 * 연동 도메인 (경비 / 휴가 등) 은 refId(Long) 로만 연결되고, 최종 승인 / 반려 / 취소 시
 * ApprovalResultHandler SPI 로 통지받는다. GENERAL 기안은 refId 없이 문서 자체가 전부.
 */
@Entity
@Getter
@Table(name = "approval_documents",
        indexes = @Index(name = "idx_approval_documents_drafter_id", columnList = "drafter_id"))
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ApprovalDocument extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "doc_type", nullable = false, comment = "문서 유형")
    private ApprovalDocType docType;

    @Column(nullable = false, comment = "제목")
    private String title;

    @Column(columnDefinition = "TEXT", comment = "본문")
    private String content;

    @Column(name = "drafter_id", nullable = false, comment = "기안자 직원 식별자")
    private Long drafterId;

    @Column(name = "ref_id", comment = "연동 도메인 레코드 식별자 — GENERAL 기안은 null")
    private Long refId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, comment = "문서 상태")
    private ApprovalStatus status;

    @Column(name = "current_step_order", nullable = false, comment = "현재 결재 차례 — 1부터")
    private int currentStepOrder;

    // 동시 결정 (같은 문서의 승인 / 반려 동시 요청) 을 낙관적 락으로 방어
    @Version
    private long version;

    @ElementCollection
    @CollectionTable(name = "approval_document_attachments", joinColumns = @JoinColumn(name = "document_id"))
    @OrderColumn(name = "attachment_order")
    @Column(name = "file_id", nullable = false)
    private List<Long> attachmentFileIds = new ArrayList<>();

    @OneToMany(mappedBy = "document", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("stepOrder ASC")
    private List<ApprovalStep> steps = new ArrayList<>();

    @Builder
    ApprovalDocument(ApprovalDocType docType,
                     String title,
                     String content,
                     Long drafterId,
                     Long refId,
                     List<Long> attachmentFileIds) {
        this.docType = docType;
        this.title = title;
        this.content = content;
        this.drafterId = drafterId;
        this.refId = refId;
        this.attachmentFileIds = attachmentFileIds == null ? new ArrayList<>() : new ArrayList<>(attachmentFileIds);
        this.status = ApprovalStatus.IN_PROGRESS;
        this.currentStepOrder = 1;
    }

    /** 결재선 구성 — 추가 순서 그대로 1단계부터 부여. */
    public void addStep(Long approverId) {
        steps.add(ApprovalStep.builder()
                .document(this)
                .stepOrder(steps.size() + 1)
                .approverId(approverId)
                .build());
    }

    /** 현재 차례의 결재 단계 — IN_PROGRESS 문서는 항상 존재. */
    public ApprovalStep currentStep() {
        return steps.stream()
                .filter(step -> step.getStepOrder() == currentStepOrder)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("현재 차례의 결재 단계가 없습니다."));
    }

    public boolean isCompleted() {
        return status != ApprovalStatus.IN_PROGRESS;
    }

    public boolean isLastStep() {
        return currentStepOrder >= steps.size();
    }

    public boolean isAllStepsPending() {
        return steps.stream().allMatch(step -> step.getStatus() == StepStatus.PENDING);
    }

    /** 관련자 여부 — 기안자이거나 결재선에 포함. */
    public boolean involves(Long employeeId) {
        return drafterId.equals(employeeId)
                || steps.stream().anyMatch(step -> step.getApproverId().equals(employeeId));
    }

    public void advanceStep() {
        this.currentStepOrder++;
    }

    public void markApproved() {
        this.status = ApprovalStatus.APPROVED;
    }

    public void markRejected() {
        this.status = ApprovalStatus.REJECTED;
    }

    public void markCanceled() {
        this.status = ApprovalStatus.CANCELED;
    }
}
