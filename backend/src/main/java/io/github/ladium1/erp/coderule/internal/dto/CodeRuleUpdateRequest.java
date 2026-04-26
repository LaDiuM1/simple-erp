package io.github.ladium1.erp.coderule.internal.dto;

import io.github.ladium1.erp.coderule.api.InputMode;
import io.github.ladium1.erp.coderule.api.ResetPolicy;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CodeRuleUpdateRequest(
        @Size(max = 50)
        String prefix,

        @NotBlank @Size(max = 200)
        String pattern,

        @NotNull @Min(1) @Max(18)
        Integer defaultSeqLength,

        @NotNull
        ResetPolicy resetPolicy,

        @NotNull
        InputMode inputMode,

        boolean parentScoped,

        @Size(max = 500)
        String description
) {
}
