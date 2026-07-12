package io.github.ladium1.erp.contract.internal.dto;

import io.github.ladium1.erp.contract.internal.entity.ContractStatus;
import io.github.ladium1.erp.contract.internal.entity.OutputUnit;
import io.github.ladium1.erp.contract.internal.entity.SupportProgramStatus;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 엑셀 다운로드 전용 행 — 목록 응답보다 넓게 마일스톤 일자 / 금액 전체를 포함한다
 * (영업 보고용 파일을 수작업 필터링으로 만들던 업무 대체 목적).
 */
@Builder
public record ContractExcelRow(
        String contractNo,
        String customerName,
        String employeeName,
        String supplierName,
        String categoryName,
        String productModelName,
        BigDecimal outputValue,
        OutputUnit outputUnit,
        String optionText,
        String cretopGrade,
        String supportProgramName,
        SupportProgramStatus supportProgramStatus,
        ContractStatus status,
        LocalDate contractDate,
        LocalDate dueDate,
        LocalDate orderDate,
        LocalDate expectedArrivalDate,
        LocalDate arrivalDate,
        LocalDate installedDate,
        LocalDate settledDate,
        Long initialAmount,
        Long finalAmount,
        Long paidTotal,
        Long outstandingAmount,
        String logisticsNote
) {
}
