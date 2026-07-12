package io.github.ladium1.erp.contract.internal.dto;

import io.github.ladium1.erp.contract.internal.entity.ContractStatus;
import io.github.ladium1.erp.contract.internal.entity.OutputUnit;
import io.github.ladium1.erp.contract.internal.entity.SupportProgramStatus;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;

@Builder
public record ContractSummaryResponse(
        Long id,
        String contractNo,
        Long customerId,
        String customerName,
        Long employeeId,
        String employeeName,
        Long supplierId,
        String supplierName,
        Long productId,
        String productModelName,
        String categoryName,
        BigDecimal outputValue,
        OutputUnit outputUnit,
        Long finalAmount,
        /** 최종 계약금액 − Σ입금액 — 서버 자동 산출 */
        Long outstandingAmount,
        LocalDate contractDate,
        LocalDate dueDate,
        String supportProgramName,
        SupportProgramStatus supportProgramStatus,
        ContractStatus status
) {
}
