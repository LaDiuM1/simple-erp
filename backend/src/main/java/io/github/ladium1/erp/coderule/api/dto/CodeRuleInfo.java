package io.github.ladium1.erp.coderule.api.dto;

import io.github.ladium1.erp.coderule.api.CodeRuleTarget;
import io.github.ladium1.erp.coderule.api.InputMode;
import io.github.ladium1.erp.coderule.api.ResetPolicy;
import lombok.Builder;

@Builder
public record CodeRuleInfo(
        CodeRuleTarget target,
        String prefix,
        String pattern,
        Integer defaultSeqLength,
        ResetPolicy resetPolicy,
        InputMode inputMode,
        boolean parentScoped,
        String description
) {
}
