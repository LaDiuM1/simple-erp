package io.github.ladium1.erp.contract.internal.dto;

import io.github.ladium1.erp.contract.internal.entity.ContractStatus;
import io.github.ladium1.erp.contract.internal.entity.OutputUnit;
import io.github.ladium1.erp.contract.internal.entity.SupportProgramStatus;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Builder
public record ContractDetailResponse(
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
        String optionText,
        Long initialAmount,
        Long finalAmount,
        /** Σ입금액 — 서버 자동 산출 */
        Long paidTotal,
        /** 최종 계약금액 − Σ입금액 — 서버 자동 산출 */
        Long outstandingAmount,
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
        ContractStatus status,
        List<ContractPaymentResponse> payments,
        List<ContractNoteResponse> notes
) {
}
