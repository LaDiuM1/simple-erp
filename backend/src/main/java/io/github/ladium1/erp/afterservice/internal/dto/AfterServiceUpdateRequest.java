package io.github.ladium1.erp.afterservice.internal.dto;

import io.github.ladium1.erp.afterservice.internal.entity.ServiceStatus;
import io.github.ladium1.erp.afterservice.internal.entity.ServiceType;
import io.github.ladium1.erp.afterservice.internal.entity.WarrantyDecision;
import io.github.ladium1.erp.global.validation.MoneyPolicy;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

/**
 * AS 수정 요청 — 접수번호는 발급 후 불변이라 수정 항목에 없다.
 */
public record AfterServiceUpdateRequest(
        @NotNull
        Long customerId,

        Long equipmentId,

        @NotNull
        LocalDate receivedDate,

        @NotNull
        ServiceType type,

        @Size(max = 4000)
        String symptom,

        @NotNull
        ServiceStatus status,

        Long assignedEngineerId,

        @NotNull
        WarrantyDecision warrantyDecision,

        @PositiveOrZero @Max(MoneyPolicy.MAX_AMOUNT)
        Long billingAmount,

        LocalDate completedDate
) {
}
