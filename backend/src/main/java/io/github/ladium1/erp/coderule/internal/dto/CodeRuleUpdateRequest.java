package io.github.ladium1.erp.coderule.internal.dto;

import io.github.ladium1.erp.coderule.api.InputMode;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record CodeRuleUpdateRequest(
        @NotBlank @Size(max = 200)
        String pattern,

        @NotNull
        InputMode inputMode,

        @Size(max = 500)
        String description,

        /** 분류값 매핑 — null 이면 유지, 빈 리스트면 전체 삭제, 값이 있으면 전체 교체. */
        @Valid
        List<CodeRuleAttributeMappingPayload> attributeMappings
) {
}
