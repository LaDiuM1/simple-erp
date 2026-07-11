package io.github.ladium1.erp.afterservice.internal.entity;

import io.github.ladium1.erp.global.jpa.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * AS 경비 — AS 건의 원가 기록 (일당 / 숙박비 / 식대 / 부품비 / 기타).
 * <p>
 * 엔지니어 숙소 결제 내역은 "분류 = 숙박비 + 결제주체 = 회사" 행으로 흡수 (별도 숙박 시트 대체).
 * 직원 개인 경비 청구 + 결재 흐름인 expense (지출결의) 모듈과는 목적이 달라 혼용하지 않는다.
 */
@Entity
@Getter
@Table(name = "service_expenses",
        indexes = {
                @Index(name = "idx_service_expenses_after_service_id", columnList = "after_service_id"),
                @Index(name = "idx_service_expenses_engineer_id", columnList = "engineer_id")
        })
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ServiceExpense extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "after_service_id", nullable = false,
            comment = "AS 건 식별자")
    private Long afterServiceId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false,
            comment = "경비 분류 (일당 / 숙박비 / 식대 / 부품비 / 기타)")
    private ServiceExpenseCategory category;

    @Column(nullable = false,
            comment = "금액 (원)")
    private Long amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "payer_type", nullable = false,
            comment = "결제 주체 (회사 직접결제 / 엔지니어 청구)")
    private ExpensePayerType payerType;

    @Column(name = "paid_date",
            comment = "결제일")
    private LocalDate paidDate;

    @Column(name = "engineer_id",
            comment = "관련 엔지니어 — 부품비 등 엔지니어 무관 경비는 null")
    private Long engineerId;

    @Column(comment = "메모")
    private String note;

    @Builder
    ServiceExpense(Long afterServiceId,
                   ServiceExpenseCategory category,
                   Long amount,
                   ExpensePayerType payerType,
                   LocalDate paidDate,
                   Long engineerId,
                   String note) {
        this.afterServiceId = afterServiceId;
        this.category = category;
        this.amount = amount;
        this.payerType = payerType;
        this.paidDate = paidDate;
        this.engineerId = engineerId;
        this.note = note;
    }

    public void update(ServiceExpenseCategory category,
                       Long amount,
                       ExpensePayerType payerType,
                       LocalDate paidDate,
                       Long engineerId,
                       String note) {
        this.category = category;
        this.amount = amount;
        this.payerType = payerType;
        this.paidDate = paidDate;
        this.engineerId = engineerId;
        this.note = note;
    }
}
