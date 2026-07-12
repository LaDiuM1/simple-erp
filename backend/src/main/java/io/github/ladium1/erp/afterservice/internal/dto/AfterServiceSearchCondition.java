package io.github.ladium1.erp.afterservice.internal.dto;

import io.github.ladium1.erp.afterservice.internal.entity.ServiceStatus;
import io.github.ladium1.erp.afterservice.internal.entity.ServiceType;
import io.github.ladium1.erp.afterservice.internal.entity.WarrantyDecision;

import java.time.LocalDate;

public record AfterServiceSearchCondition(
        String receiptNoKeyword,
        Long customerId,
        ServiceType type,
        ServiceStatus status,
        WarrantyDecision warrantyDecision,
        Long engineerId,
        LocalDate receivedDateFrom,
        LocalDate receivedDateTo
) {
}
