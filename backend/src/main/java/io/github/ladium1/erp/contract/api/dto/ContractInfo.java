package io.github.ladium1.erp.contract.api.dto;

import lombok.Builder;

import java.time.LocalDate;

@Builder
public record ContractInfo(
        Long id,
        String contractNo,
        Long customerId,
        LocalDate contractDate
) {
}
