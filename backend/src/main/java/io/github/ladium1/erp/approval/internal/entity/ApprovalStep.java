package io.github.ladium1.erp.approval.internal.entity;

import io.github.ladium1.erp.global.jpa.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 결재 단계 — 결재선의 한 칸.
 * <p>
 * stepOrder 는 1부터 시작하며 문서의 currentStepOrder 와 일치하는 단계만 결정 가능.
 */
@Entity
@Getter
@Table(name = "approval_steps",
        indexes = @Index(name = "idx_approval_steps_approver_id", columnList = "approver_id"),
        uniqueConstraints = @UniqueConstraint(
                name = "uk_approval_steps_document_step_order",
                columnNames = {"document_id", "step_order"}
        ))
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ApprovalStep extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "document_id")
    private ApprovalDocument document;

    @Column(name = "step_order", nullable = false, comment = "결재 순번 — 1부터")
    private int stepOrder;

    @Column(name = "approver_id", nullable = false, comment = "결재자 직원 식별자")
    private Long approverId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, comment = "단계 상태")
    private StepStatus status;

    @Column(comment = "결재 의견")
    private String comment;

    @Column(name = "decided_at", comment = "결정 일시")
    private LocalDateTime decidedAt;

    @Builder
    ApprovalStep(ApprovalDocument document, int stepOrder, Long approverId) {
        this.document = document;
        this.stepOrder = stepOrder;
        this.approverId = approverId;
        this.status = StepStatus.PENDING;
    }

    public void approve(String comment) {
        decide(StepStatus.APPROVED, comment);
    }

    public void reject(String comment) {
        decide(StepStatus.REJECTED, comment);
    }

    private void decide(StepStatus status, String comment) {
        this.status = status;
        this.comment = comment;
        this.decidedAt = LocalDateTime.now();
    }
}
