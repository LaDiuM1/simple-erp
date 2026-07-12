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

/**
 * 계약 변경 이력 메모 — 계약일 변경 / 설비 변경 등을 셀 안에 줄바꿈으로 누적하던 엑셀 실무를
 * 정규화된 이력 행으로 대체. 작성 시각은 BaseEntity.createdAt, 수정은 없고 등록 / 삭제만 허용.
 */
@Entity
@Getter
@Table(name = "contract_notes",
        indexes = @Index(name = "idx_contract_notes_contract_id", columnList = "contract_id"))
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ContractNote extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "contract_id", nullable = false,
            comment = "계약 식별자")
    private Long contractId;

    @Column(name = "author_employee_id", nullable = false,
            comment = "작성자 직원 참조 (employee 모듈)")
    private Long authorEmployeeId;

    @Column(nullable = false, columnDefinition = "TEXT",
            comment = "메모 내용")
    private String content;

    @Builder
    ContractNote(Long contractId, Long authorEmployeeId, String content) {
        this.contractId = contractId;
        this.authorEmployeeId = authorEmployeeId;
        this.content = content;
    }
}
