package io.github.ladium1.erp.customer.internal.dto;

import io.github.ladium1.erp.customer.internal.entity.CustomerStatus;
import io.github.ladium1.erp.customer.internal.entity.CustomerType;
import lombok.Builder;

import java.time.LocalDate;

@Builder
public record CustomerDetailResponse(
        Long id,
        String code,
        String name,
        String nameEn,
        String bizRegNo,
        String corpRegNo,
        String representative,
        String bizType,
        String bizItem,
        String phone,
        String fax,
        String email,
        String website,
        String zipCode,
        String roadAddress,
        String detailAddress,
        CustomerType type,
        CustomerStatus status,
        LocalDate tradeStartDate,
        String note
) {
}
