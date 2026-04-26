package io.github.ladium1.erp.customer.api.dto;

import lombok.Builder;

@Builder
public record CustomerInfo(
        Long id,
        String code,
        String name,
        String bizRegNo
) {
}
