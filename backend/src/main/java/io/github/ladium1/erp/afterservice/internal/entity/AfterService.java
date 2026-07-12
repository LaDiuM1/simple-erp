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
 * AS 건 — 접수부터 완료까지의 단위 (수 주짜리 설치 출장도 한 건 아래 방문 일지로 누적).
 * <p>
 * 설비 대장 (equipmentId) 이 연결되면 보증 만료일 기반 유상 / 무상 자동 제안의 근거가 되고,
 * 대장 미등록 설비 (과거 판매분 등) 는 equipmentId 없이 접수를 허용한다.
 * 유상 확정 건은 청구액 (billingAmount) 을 기록 — 원가 (경비) 만 남기면 매출 측면이 빠지는 문제 방지.
 */
@Entity
@Getter
@Table(name = "after_services",
        indexes = {
                @Index(name = "idx_after_services_customer_id", columnList = "customer_id"),
                @Index(name = "idx_after_services_equipment_id", columnList = "equipment_id")
        })
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AfterService extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "receipt_no", nullable = false, unique = true,
            comment = "AS 접수번호 (채번 규칙 AFTER_SERVICE)")
    private String receiptNo;

    @Column(name = "customer_id", nullable = false,
            comment = "고객사 참조 (customer 모듈)")
    private Long customerId;

    @Column(name = "equipment_id",
            comment = "설비 대장 참조 (equipment 모듈) — 대장 미등록 설비 접수는 null")
    private Long equipmentId;

    @Column(name = "received_date", nullable = false,
            comment = "접수일")
    private LocalDate receivedDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false,
            comment = "AS 유형 (수리 / 설치지원 / 교육 / 통역 / 조건셋팅)")
    private ServiceType type;

    @Column(columnDefinition = "TEXT",
            comment = "증상 / 요청 내용")
    private String symptom;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false,
            comment = "진행 상태 (접수 / 배정 / 진행중 / 완료)")
    private ServiceStatus status;

    @Column(name = "assigned_engineer_id",
            comment = "주 담당 엔지니어 — 방문 일지는 엔지니어별 별도 기록")
    private Long assignedEngineerId;

    @Enumerated(EnumType.STRING)
    @Column(name = "warranty_decision", nullable = false,
            comment = "유상 / 무상 판정 — 설비 보증 기반 제안 + 담당자 확정")
    private WarrantyDecision warrantyDecision;

    @Column(name = "billing_amount",
            comment = "유상 청구액 (원, VAT 별도) — 유상 확정 건만")
    private Long billingAmount;

    @Column(name = "completed_date",
            comment = "완료일")
    private LocalDate completedDate;

    @Builder
    AfterService(String receiptNo,
                 Long customerId,
                 Long equipmentId,
                 LocalDate receivedDate,
                 ServiceType type,
                 String symptom,
                 ServiceStatus status,
                 Long assignedEngineerId,
                 WarrantyDecision warrantyDecision,
                 Long billingAmount,
                 LocalDate completedDate) {
        this.receiptNo = receiptNo;
        this.customerId = customerId;
        this.equipmentId = equipmentId;
        this.receivedDate = receivedDate;
        this.type = type;
        this.symptom = symptom;
        this.status = status;
        this.assignedEngineerId = assignedEngineerId;
        this.warrantyDecision = warrantyDecision;
        this.billingAmount = billingFor(warrantyDecision, billingAmount);
        this.completedDate = completedDate;
    }

    public void update(Long customerId,
                       Long equipmentId,
                       LocalDate receivedDate,
                       ServiceType type,
                       String symptom,
                       ServiceStatus status,
                       Long assignedEngineerId,
                       WarrantyDecision warrantyDecision,
                       Long billingAmount,
                       LocalDate completedDate) {
        this.customerId = customerId;
        this.equipmentId = equipmentId;
        this.receivedDate = receivedDate;
        this.type = type;
        this.symptom = symptom;
        this.status = status;
        this.assignedEngineerId = assignedEngineerId;
        this.warrantyDecision = warrantyDecision;
        this.billingAmount = billingFor(warrantyDecision, billingAmount);
        this.completedDate = completedDate;
    }

    /** 청구액은 유상 (PAID) 확정 건에만 유효 — 무상 / 미확정 판정엔 청구액을 남기지 않아 모순 데이터를 차단. */
    private static Long billingFor(WarrantyDecision warrantyDecision, Long billingAmount) {
        return warrantyDecision == WarrantyDecision.PAID ? billingAmount : null;
    }
}
