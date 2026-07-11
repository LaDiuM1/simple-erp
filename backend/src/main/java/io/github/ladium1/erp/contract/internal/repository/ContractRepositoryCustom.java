package io.github.ladium1.erp.contract.internal.repository;

import io.github.ladium1.erp.contract.api.dto.ContractOutstandingSummary;
import io.github.ladium1.erp.contract.api.dto.MonthlyContractStat;
import io.github.ladium1.erp.contract.internal.dto.ContractSearchCondition;
import io.github.ladium1.erp.contract.internal.entity.Contract;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

public interface ContractRepositoryCustom {

    Page<Contract> search(ContractSearchCondition condition, Pageable pageable);

    List<Contract> searchAll(ContractSearchCondition condition, Sort sort);

    /**
     * 계약일 기준 월별 건수 / Σ최종 계약금액 — 계약취소 제외. 데이터 없는 달은 미포함 (서비스가 0 채움).
     * employeeIdScope null = 제한 없음.
     */
    List<MonthlyContractStat> monthlyStats(LocalDate fromDate, Set<Long> employeeIdScope);

    /**
     * 수금 vs 미수 누적 — 계약취소 제외. employeeIdScope null = 제한 없음.
     */
    ContractOutstandingSummary outstandingSummary(Set<Long> employeeIdScope);
}
