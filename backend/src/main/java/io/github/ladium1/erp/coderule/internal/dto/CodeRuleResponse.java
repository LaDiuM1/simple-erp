package io.github.ladium1.erp.coderule.internal.dto;

import io.github.ladium1.erp.coderule.api.CodeRuleTarget;
import io.github.ladium1.erp.coderule.api.InputMode;
import io.github.ladium1.erp.coderule.api.ResetPolicy;
import lombok.Builder;

/**
 * 채번 규칙 관리 화면용 응답. CodeRuleInfo 에 라벨과 다음 코드 미리보기를 덧붙인다.
 */
@Builder
public record CodeRuleResponse(
        Long id,
        CodeRuleTarget target,
        String targetLabel,
        String prefix,
        String pattern,
        Integer defaultSeqLength,
        ResetPolicy resetPolicy,
        InputMode inputMode,
        boolean parentScoped,
        String description,
        String nextCode
) {
}
