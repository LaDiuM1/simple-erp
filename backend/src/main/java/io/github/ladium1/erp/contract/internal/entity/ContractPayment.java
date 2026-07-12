package io.github.ladium1.erp.contract.internal.entity;

import io.github.ladium1.erp.global.jpa.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
 * 계약 대금 스케줄 회차 — 계약금 / 중도금 / 잔금 구조 (라벨 자유 입력, 회차 추가 제한 없음).
 * <p>
 * 회차별 예정 / 입금 / 세금계산서를 분리 컬럼으로 기록 — 미수금은 최종 계약금액 − Σ입금액으로
 * 자동 산출한다 (엑셀의 수기 대조 대체). 지원금 입금 연동 같은 특이 사항은 note 에 기재.
 */
@Entity
@Getter
@Table(name = "contract_payments",
        indexes = @Index(name = "idx_contract_payments_contract_id", columnList = "contract_id"))
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ContractPayment extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "contract_id", nullable = false,
            comment = "계약 식별자")
    private Long contractId;

    @Column(nullable = false, length = 50,
            comment = "회차 라벨 (계약금 / 중도금 / 잔금 등 자유 입력)")
    private String label;

    @Column(name = "planned_date",
            comment = "입금 예정일")
    private LocalDate plannedDate;

    @Column(name = "planned_amount",
            comment = "예정 금액 (원, VAT 별도)")
    private Long plannedAmount;

    @Column(name = "paid_date",
            comment = "입금일")
    private LocalDate paidDate;

    @Column(name = "paid_amount",
            comment = "입금액 (원, VAT 별도)")
    private Long paidAmount;

    @Column(name = "invoice_date",
            comment = "세금계산서 발행일")
    private LocalDate invoiceDate;

    @Column(name = "invoice_amount",
            comment = "세금계산서 금액 (원, VAT 별도)")
    private Long invoiceAmount;

    @Column(comment = "메모 (지원금 입금 연동 등)")
    private String note;

    @Builder
    ContractPayment(Long contractId,
                    String label,
                    LocalDate plannedDate,
                    Long plannedAmount,
                    LocalDate paidDate,
                    Long paidAmount,
                    LocalDate invoiceDate,
                    Long invoiceAmount,
                    String note) {
        this.contractId = contractId;
        this.label = label;
        this.plannedDate = plannedDate;
        this.plannedAmount = plannedAmount;
        this.paidDate = paidDate;
        this.paidAmount = paidAmount;
        this.invoiceDate = invoiceDate;
        this.invoiceAmount = invoiceAmount;
        this.note = note;
    }

    public void update(String label,
                       LocalDate plannedDate,
                       Long plannedAmount,
                       LocalDate paidDate,
                       Long paidAmount,
                       LocalDate invoiceDate,
                       Long invoiceAmount,
                       String note) {
        this.label = label;
        this.plannedDate = plannedDate;
        this.plannedAmount = plannedAmount;
        this.paidDate = paidDate;
        this.paidAmount = paidAmount;
        this.invoiceDate = invoiceDate;
        this.invoiceAmount = invoiceAmount;
        this.note = note;
    }
}
