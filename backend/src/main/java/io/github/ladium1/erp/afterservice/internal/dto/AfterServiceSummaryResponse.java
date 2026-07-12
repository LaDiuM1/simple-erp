package io.github.ladium1.erp.afterservice.internal.dto;

import io.github.ladium1.erp.afterservice.internal.entity.ServiceStatus;
import io.github.ladium1.erp.afterservice.internal.entity.ServiceType;
import io.github.ladium1.erp.afterservice.internal.entity.WarrantyDecision;
import lombok.Builder;

import java.time.LocalDate;

@Builder
public record AfterServiceSummaryResponse(
        Long id,
        String receiptNo,
        Long customerId,
        String customerName,
        Long equipmentId,
        String equipmentModelName,
        String equipmentSerialNo,
        LocalDate receivedDate,
        ServiceType type,
        ServiceStatus status,
        Long assignedEngineerId,
        String assignedEngineerName,
        WarrantyDecision warrantyDecision,
        Long billingAmount,
        /** Σ경비 — 서버 자동 산출 */
        Long expenseTotal,
        LocalDate completedDate
) {
}
