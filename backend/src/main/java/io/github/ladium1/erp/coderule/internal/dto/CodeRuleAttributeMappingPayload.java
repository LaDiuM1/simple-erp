package io.github.ladium1.erp.coderule.internal.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 분류값 매핑 입력 (1줄 = 1 sourceValue) — 채번 규칙 저장 요청에 포함.
 */
public record CodeRuleAttributeMappingPayload(
        @NotBlank @Size(max = 50) String attributeKey,
        @NotBlank @Size(max = 100) String sourceValue,
        @NotBlank @Size(max = 50) String codeValue
) {
}
