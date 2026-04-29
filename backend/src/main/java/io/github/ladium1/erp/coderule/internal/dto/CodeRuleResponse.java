package io.github.ladium1.erp.coderule.internal.dto;

import io.github.ladium1.erp.coderule.api.CodeRuleTarget;
import io.github.ladium1.erp.coderule.api.InputMode;
import lombok.Builder;

/**
 * 채번 규칙 관리 화면용 응답.
 */
@Builder
public record CodeRuleResponse(
        Long id,
        CodeRuleTarget target,
        String targetLabel,
        String pattern,
        InputMode inputMode,
        boolean hasParent,
        String description,
        /** 분류 토큰 / PARENT 토큰을 사용하면 단순 다음 코드 미리보기 불가 -> null */
        String nextCode
) {
}
