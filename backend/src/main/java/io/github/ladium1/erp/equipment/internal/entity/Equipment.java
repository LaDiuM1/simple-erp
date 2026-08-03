package io.github.ladium1.erp.equipment.internal.entity;

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
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 설비 대장 — 설치 완료된 설비 1대 = 1행 (엑셀 "설비 설치완료 업체리스트" 대체).
 * 계약과 AS 를 잇는 허브 — AS 접수 시 이 행의 보증 만료일로 유상 / 무상을 자동 판정한다.
 * <p>
 * 보증 만료일은 기산일 + 개월 수에서 파생되지만, 만료 임박 필터를 SQL 로 걸 수 있도록
 * 쓰기 시점에 계산해 컬럼으로 저장한다 (기산일 / 개월 변경 시 함께 재계산).
 */
@Entity
@Getter
@Table(name = "equipments",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_equipments_contract_id", columnNames = "contract_id"),
        indexes = {
                @Index(name = "idx_equipments_customer_id", columnList = "customer_id"),
                @Index(name = "idx_equipments_product_id", columnList = "product_id")
        })
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Equipment extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "customer_id", nullable = false,
            comment = "고객사 참조 (customer 모듈)")
    private Long customerId;

    @Column(name = "contract_id",
            comment = "원천 계약 참조 (contract 모듈) — 계약 설치완료 자동 생성분만 채워짐, 과거 이관분은 null")
    private Long contractId;

    @Column(name = "supplier_id", nullable = false,
            comment = "공급사 참조 (supplier 모듈) — 등록 시점 제품의 공급사 스냅샷")
    private Long supplierId;

    @Column(name = "product_id", nullable = false,
            comment = "제품 모델 참조 (product 모듈)")
    private Long productId;

    @Column(name = "output_value", precision = 10, scale = 2,
            comment = "출력 값 (예: 12 kW, 220 ton)")
    private BigDecimal outputValue;

    @Enumerated(EnumType.STRING)
    @Column(name = "output_unit",
            comment = "출력 단위 (KW / TON)")
    private OutputUnit outputUnit;

    @Column(name = "serial_no", length = 100,
            comment = "시리얼 번호 (명판) — 미확인 설비는 null")
    private String serialNo;

    @Column(name = "install_address",
            comment = "설치 주소 — 고객사 주소와 다른 실사례가 있어 별도 보유")
    private String installAddress;

    @Column(name = "installed_date",
            comment = "설치일")
    private LocalDate installedDate;

    @Column(name = "confirmed_date",
            comment = "설치완료확인서 (교육 포함) 일자")
    private LocalDate confirmedDate;

    @Column(name = "warranty_start_date",
            comment = "보증 기산일")
    private LocalDate warrantyStartDate;

    @Column(name = "oscillator_warranty_months",
            comment = "발진기 (레이저 소스) 보증 개월 — 계약서 기준 24~60")
    private Integer oscillatorWarrantyMonths;

    @Column(name = "general_warranty_months",
            comment = "발진기 외 무상 AS 개월 — 계약서 기준 0~30")
    private Integer generalWarrantyMonths;

    @Column(name = "oscillator_warranty_end_date",
            comment = "발진기 보증 만료일 — 기산일 + 개월 파생값 (필터용 저장)")
    private LocalDate oscillatorWarrantyEndDate;

    @Column(name = "general_warranty_end_date",
            comment = "무상 AS 만료일 — 기산일 + 개월 파생값 (필터용 저장)")
    private LocalDate generalWarrantyEndDate;

    @Column(name = "warranty_insurance", nullable = false,
            comment = "보증보험 가입 여부")
    private boolean warrantyInsurance;

    @Column(columnDefinition = "TEXT", comment = "비고")
    private String note;

    @Builder
    Equipment(Long customerId,
              Long contractId,
              Long supplierId,
              Long productId,
              BigDecimal outputValue,
              OutputUnit outputUnit,
              String serialNo,
              String installAddress,
              LocalDate installedDate,
              LocalDate confirmedDate,
              LocalDate warrantyStartDate,
              Integer oscillatorWarrantyMonths,
              Integer generalWarrantyMonths,
              boolean warrantyInsurance,
              String note) {
        this.customerId = customerId;
        this.contractId = contractId;
        this.supplierId = supplierId;
        this.productId = productId;
        this.outputValue = outputValue;
        this.outputUnit = outputUnit;
        this.serialNo = serialNo;
        this.installAddress = installAddress;
        this.installedDate = installedDate;
        this.confirmedDate = confirmedDate;
        this.warrantyStartDate = warrantyStartDate;
        this.oscillatorWarrantyMonths = oscillatorWarrantyMonths;
        this.generalWarrantyMonths = generalWarrantyMonths;
        this.warrantyInsurance = warrantyInsurance;
        this.note = note;
        recalculateWarrantyEndDates();
    }

    public void update(Long customerId,
                       Long supplierId,
                       Long productId,
                       BigDecimal outputValue,
                       OutputUnit outputUnit,
                       String serialNo,
                       String installAddress,
                       LocalDate installedDate,
                       LocalDate confirmedDate,
                       LocalDate warrantyStartDate,
                       Integer oscillatorWarrantyMonths,
                       Integer generalWarrantyMonths,
                       boolean warrantyInsurance,
                       String note) {
        this.customerId = customerId;
        this.supplierId = supplierId;
        this.productId = productId;
        this.outputValue = outputValue;
        this.outputUnit = outputUnit;
        this.serialNo = serialNo;
        this.installAddress = installAddress;
        this.installedDate = installedDate;
        this.confirmedDate = confirmedDate;
        this.warrantyStartDate = warrantyStartDate;
        this.oscillatorWarrantyMonths = oscillatorWarrantyMonths;
        this.generalWarrantyMonths = generalWarrantyMonths;
        this.warrantyInsurance = warrantyInsurance;
        this.note = note;
        recalculateWarrantyEndDates();
    }

    private void recalculateWarrantyEndDates() {
        this.oscillatorWarrantyEndDate = warrantyEnd(oscillatorWarrantyMonths);
        this.generalWarrantyEndDate = warrantyEnd(generalWarrantyMonths);
    }

    private LocalDate warrantyEnd(Integer months) {
        if (warrantyStartDate == null || months == null) {
            return null;
        }
        return warrantyStartDate.plusMonths(months);
    }
}
