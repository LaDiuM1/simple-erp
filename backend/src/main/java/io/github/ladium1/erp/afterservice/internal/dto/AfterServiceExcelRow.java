package io.github.ladium1.erp.afterservice.internal.dto;

import io.github.ladium1.erp.afterservice.internal.entity.ServiceStatus;
import io.github.ladium1.erp.afterservice.internal.entity.ServiceType;
import io.github.ladium1.erp.afterservice.internal.entity.WarrantyDecision;
import lombok.Builder;

import java.time.LocalDate;

/**
 * 엑셀 다운로드 전용 행 — 서비스 리포트 집계 (건 단위) 컬럼 구성.
 */
@Builder
public record AfterServiceExcelRow(
        String receiptNo,
        String customerName,
        String equipmentModelName,
        String equipmentSerialNo,
        ServiceType type,
        ServiceStatus status,
        LocalDate receivedDate,
        LocalDate completedDate,
        String assignedEngineerName,
        WarrantyDecision warrantyDecision,
        Long billingAmount,
        Long expenseTotal,
        String symptom
) {
}
