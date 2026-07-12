package io.github.ladium1.erp.contract.internal.entity;

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

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 설비 계약 — 계약 1건 = 설비 1대 (엑셀 설비계약현황 1행과 동일 단위).
 * <p>
 * 고객 / 계약자 / 공급사 / 제품은 텍스트 재기입 대신 참조만 보유 (정규화 목적).
 * supplierId 는 계약 시점 제품의 공급사를 저장한 스냅샷 — 이후 제품 마스터의 공급사가
 * 바뀌어도 계약 이력은 계약 당시 공급사를 유지한다.
 * 마일스톤 일자는 각각 개별 컬럼 — 엑셀에서 한 셀에 변경 이력 / 컨테이너 정보가 혼기되던 문제를 해소.
 */
@Entity
@Getter
@Table(name = "contracts",
        indexes = {
                @Index(name = "idx_contracts_customer_id", columnList = "customer_id"),
                @Index(name = "idx_contracts_employee_id", columnList = "employee_id"),
                @Index(name = "idx_contracts_product_id", columnList = "product_id")
        })
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Contract extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "contract_no", nullable = false, unique = true,
            comment = "계약 번호 (채번 규칙 CONTRACT)")
    private String contractNo;

    @Column(name = "customer_id", nullable = false,
            comment = "고객사 참조 (customer 모듈)")
    private Long customerId;

    @Column(name = "employee_id", nullable = false,
            comment = "계약자 (영업 담당) 참조 (employee 모듈)")
    private Long employeeId;

    @Column(name = "supplier_id", nullable = false,
            comment = "공급사 참조 (supplier 모듈) — 계약 시점 제품의 공급사 스냅샷")
    private Long supplierId;

    @Column(name = "product_id", nullable = false,
            comment = "제품 모델 참조 (product 모듈)")
    private Long productId;

    @Column(name = "output_value", precision = 10, scale = 2,
            comment = "출력 값 (예: 12 kW, 220 ton) — 계약별 사양")
    private BigDecimal outputValue;

    @Enumerated(EnumType.STRING)
    @Column(name = "output_unit",
            comment = "출력 단위 (KW / TON)")
    private OutputUnit outputUnit;

    @Column(name = "option_text", columnDefinition = "TEXT",
            comment = "옵션 사양 (BEVEL, FMC, CHUCK 구성 등) — 계약별 자유 기재")
    private String optionText;

    @Column(name = "initial_amount",
            comment = "초기 계약금액 (원, VAT 별도)")
    private Long initialAmount;

    @Column(name = "final_amount", nullable = false,
            comment = "최종 계약금액 (원, VAT 별도) — 협상 / 옵션 변경 반영가")
    private Long finalAmount;

    @Column(name = "cretop_grade", length = 10,
            comment = "계약 시점 CRETOP 신용등급 스냅샷 (소문자 = 모의등급)")
    private String cretopGrade;

    @Column(name = "support_program_name",
            comment = "정부 지원사업 프로그램명 (안전동행, 스마트공방 등)")
    private String supportProgramName;

    @Enumerated(EnumType.STRING)
    @Column(name = "support_program_status", nullable = false,
            comment = "지원사업 진행 상태")
    private SupportProgramStatus supportProgramStatus;

    @Column(name = "contract_date", nullable = false,
            comment = "계약일")
    private LocalDate contractDate;

    @Column(name = "due_date",
            comment = "납기일")
    private LocalDate dueDate;

    @Column(name = "order_date",
            comment = "중국 공급사 발주일")
    private LocalDate orderDate;

    @Column(name = "expected_arrival_date",
            comment = "설비 입고 예정일")
    private LocalDate expectedArrivalDate;

    @Column(name = "arrival_date",
            comment = "설비 입고일")
    private LocalDate arrivalDate;

    @Column(name = "installed_date",
            comment = "설치 완료일")
    private LocalDate installedDate;

    @Column(name = "settled_date",
            comment = "정산 완료일")
    private LocalDate settledDate;

    @Column(name = "logistics_note",
            comment = "물류 메모 (컨테이너 구성 등)")
    private String logisticsNote;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false,
            comment = "계약 진행 상태")
    private ContractStatus status;

    @Builder
    Contract(String contractNo,
             Long customerId,
             Long employeeId,
             Long supplierId,
             Long productId,
             BigDecimal outputValue,
             OutputUnit outputUnit,
             String optionText,
             Long initialAmount,
             Long finalAmount,
             String cretopGrade,
             String supportProgramName,
             SupportProgramStatus supportProgramStatus,
             LocalDate contractDate,
             LocalDate dueDate,
             LocalDate orderDate,
             LocalDate expectedArrivalDate,
             LocalDate arrivalDate,
             LocalDate installedDate,
             LocalDate settledDate,
             String logisticsNote,
             ContractStatus status) {
        this.contractNo = contractNo;
        this.customerId = customerId;
        this.employeeId = employeeId;
        this.supplierId = supplierId;
        this.productId = productId;
        this.outputValue = outputValue;
        this.outputUnit = outputUnit;
        this.optionText = optionText;
        this.initialAmount = initialAmount;
        this.finalAmount = finalAmount;
        this.cretopGrade = cretopGrade;
        this.supportProgramName = supportProgramName;
        this.supportProgramStatus = supportProgramStatus;
        this.contractDate = contractDate;
        this.dueDate = dueDate;
        this.orderDate = orderDate;
        this.expectedArrivalDate = expectedArrivalDate;
        this.arrivalDate = arrivalDate;
        this.installedDate = installedDate;
        this.settledDate = settledDate;
        this.logisticsNote = logisticsNote;
        this.status = status;
    }

    public void update(Long customerId,
                       Long employeeId,
                       Long supplierId,
                       Long productId,
                       BigDecimal outputValue,
                       OutputUnit outputUnit,
                       String optionText,
                       Long initialAmount,
                       Long finalAmount,
                       String cretopGrade,
                       String supportProgramName,
                       SupportProgramStatus supportProgramStatus,
                       LocalDate contractDate,
                       LocalDate dueDate,
                       LocalDate orderDate,
                       LocalDate expectedArrivalDate,
                       LocalDate arrivalDate,
                       LocalDate installedDate,
                       LocalDate settledDate,
                       String logisticsNote,
                       ContractStatus status) {
        this.customerId = customerId;
        this.employeeId = employeeId;
        this.supplierId = supplierId;
        this.productId = productId;
        this.outputValue = outputValue;
        this.outputUnit = outputUnit;
        this.optionText = optionText;
        this.initialAmount = initialAmount;
        this.finalAmount = finalAmount;
        this.cretopGrade = cretopGrade;
        this.supportProgramName = supportProgramName;
        this.supportProgramStatus = supportProgramStatus;
        this.contractDate = contractDate;
        this.dueDate = dueDate;
        this.orderDate = orderDate;
        this.expectedArrivalDate = expectedArrivalDate;
        this.arrivalDate = arrivalDate;
        this.installedDate = installedDate;
        this.settledDate = settledDate;
        this.logisticsNote = logisticsNote;
        this.status = status;
    }
}
