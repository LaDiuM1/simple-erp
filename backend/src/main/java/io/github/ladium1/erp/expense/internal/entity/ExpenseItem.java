package io.github.ladium1.erp.expense.internal.entity;

import io.github.ladium1.erp.global.jpa.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 경비 청구 항목 — ExpenseClaim 에 종속 (cascade + orphanRemoval).
 */
@Entity
@Getter
@Table(name = "expense_items")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ExpenseItem extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "claim_id", nullable = false)
    private ExpenseClaim claim;

    @Column(name = "expense_date", nullable = false, comment = "지출일")
    private LocalDate expenseDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, comment = "지출 분류")
    private ExpenseCategory category;

    @Column(nullable = false, precision = 15, scale = 2, comment = "지출 금액")
    private BigDecimal amount;

    @Column(comment = "지출 내용")
    private String description;

    @Column(name = "receipt_file_id", comment = "영수증 파일 식별자 — 없으면 null")
    private Long receiptFileId;

    @Builder
    ExpenseItem(ExpenseClaim claim,
                LocalDate expenseDate,
                ExpenseCategory category,
                BigDecimal amount,
                String description,
                Long receiptFileId) {
        this.claim = claim;
        this.expenseDate = expenseDate;
        this.category = category;
        this.amount = amount;
        this.description = description;
        this.receiptFileId = receiptFileId;
    }
}
