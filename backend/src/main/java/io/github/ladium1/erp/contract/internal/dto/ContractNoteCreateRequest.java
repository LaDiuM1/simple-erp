package io.github.ladium1.erp.contract.internal.dto;

import jakarta.validation.constraints.NotBlank;

public record ContractNoteCreateRequest(
        @NotBlank
        String content
) {
}
