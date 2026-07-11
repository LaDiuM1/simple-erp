package io.github.ladium1.erp.contract.internal.dto;

import io.github.ladium1.erp.contract.internal.entity.ContractStatus;

import java.time.LocalDate;
import java.util.Set;

public record ContractSearchCondition(
        String contractNoKeyword,
        Long customerId,
        Long employeeId,
        Long supplierId,
        ContractStatus status,
        LocalDate contractDateFrom,
        LocalDate contractDateTo,
        /**
         * 데이터 스코프로 좁힌 가시 계약자 (employee) ID 집합. null = 제한 없음 (ALL).
         * Controller 가 보내지 않고 service 가 합성.
         */
        Set<Long> employeeIdScope
) {
    /**
     * Controller 호환 — employeeIdScope 미지정 (ALL) 으로 시작.
     */
    public ContractSearchCondition(String contractNoKeyword, Long customerId, Long employeeId,
                                   Long supplierId, ContractStatus status,
                                   LocalDate contractDateFrom, LocalDate contractDateTo) {
        this(contractNoKeyword, customerId, employeeId, supplierId, status, contractDateFrom, contractDateTo, null);
    }

    public ContractSearchCondition withEmployeeIdScope(Set<Long> employeeIdScope) {
        return new ContractSearchCondition(contractNoKeyword, customerId, employeeId, supplierId,
                status, contractDateFrom, contractDateTo, employeeIdScope);
    }
}
