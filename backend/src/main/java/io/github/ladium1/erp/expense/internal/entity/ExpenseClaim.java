package io.github.ladium1.erp.expense.internal.entity;

import io.github.ladium1.erp.global.jpa.BaseEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * 경비 청구 — 생성 즉시 전자결재에 상신되며, 상태는 결재 결과 콜백으로만 전이.
 */
@Entity
@Getter
@Table(name = "expense_claims",
        indexes = @Index(name = "idx_expense_claims_claimant_id", columnList = "claimant_id"))
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ExpenseClaim extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "claimant_id", nullable = false, comment = "청구자 직원 식별자")
    private Long claimantId;

    @Column(nullable = false, comment = "청구 제목")
    private String title;

    @Column(name = "total_amount", nullable = false, precision = 15, scale = 2,
            comment = "총 청구 금액 — 항목 합계를 서버에서 계산")
    private BigDecimal totalAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, comment = "청구 상태")
    private ExpenseStatus status;

    @Column(name = "approval_document_id", comment = "연동된 결재 문서 식별자")
    private Long approvalDocumentId;

    @OneToMany(mappedBy = "claim", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ExpenseItem> items = new ArrayList<>();

    @Builder
    ExpenseClaim(Long claimantId, String title, BigDecimal totalAmount) {
        this.claimantId = claimantId;
        this.title = title;
        this.totalAmount = totalAmount;
        this.status = ExpenseStatus.IN_PROGRESS;
    }

    public void addItem(LocalDate expenseDate,
                        ExpenseCategory category,
                        BigDecimal amount,
                        String description,
                        Long receiptFileId) {
        items.add(ExpenseItem.builder()
                .claim(this)
                .expenseDate(expenseDate)
                .category(category)
                .amount(amount)
                .description(description)
                .receiptFileId(receiptFileId)
                .build());
    }

    public void linkApprovalDocument(Long approvalDocumentId) {
        this.approvalDocumentId = approvalDocumentId;
    }

    public void approve() {
        this.status = ExpenseStatus.APPROVED;
    }

    public void reject() {
        this.status = ExpenseStatus.REJECTED;
    }
}
