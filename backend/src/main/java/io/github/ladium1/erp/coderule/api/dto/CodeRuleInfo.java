package io.github.ladium1.erp.coderule.api.dto;

import io.github.ladium1.erp.coderule.api.CodeRuleTarget;
import io.github.ladium1.erp.coderule.api.InputMode;
import lombok.Builder;

@Builder
public record CodeRuleInfo(
        CodeRuleTarget target,
        String pattern,
        InputMode inputMode,
        String description
) {
}
