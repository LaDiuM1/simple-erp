package io.github.ladium1.erp.afterservice.internal.dto;

import io.github.ladium1.erp.afterservice.internal.entity.ServiceStatus;
import io.github.ladium1.erp.afterservice.internal.entity.ServiceType;
import io.github.ladium1.erp.afterservice.internal.entity.WarrantyDecision;
import lombok.Builder;

import java.time.LocalDate;
import java.util.List;

@Builder
public record AfterServiceDetailResponse(
        Long id,
        String receiptNo,
        Long customerId,
        String customerName,
        Long equipmentId,
        String equipmentModelName,
        String equipmentSerialNo,
        LocalDate receivedDate,
        ServiceType type,
        String symptom,
        ServiceStatus status,
        Long assignedEngineerId,
        String assignedEngineerName,
        WarrantyDecision warrantyDecision,
        Long billingAmount,
        LocalDate completedDate,
        /** Σ경비 — 서버 자동 산출 */
        Long expenseTotal,
        List<ServiceVisitResponse> visits,
        List<ServiceExpenseResponse> expenses
) {
}
