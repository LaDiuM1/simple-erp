package io.github.ladium1.erp.customer.internal.dto;

import io.github.ladium1.erp.customer.internal.entity.CustomerStatus;
import io.github.ladium1.erp.customer.internal.entity.CustomerType;
import lombok.Builder;

import java.time.LocalDate;

@Builder
public record CustomerSummaryResponse(
        Long id,
        String code,
        String name,
        String bizRegNo,
        String representative,
        String phone,
        String email,
        String roadAddress,
        CustomerType type,
        CustomerStatus status,
        LocalDate tradeStartDate
) {
}
