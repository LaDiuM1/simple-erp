package io.github.ladium1.erp.coderule.internal.dto;

import io.github.ladium1.erp.coderule.api.InputMode;
import io.github.ladium1.erp.coderule.api.ResetPolicy;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * 저장 전 미리보기용 — 사용자가 편집 중인 패턴/접두사로 다음 코드를 시뮬레이션한다.
 * <p>
 * 시퀀스는 현재 저장된 값을 기준으로 +1 한 결과를 반환하고 실제 증가는 일어나지 않는다.
 */
public record CodeRulePreviewRequest(
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

        String parentCode
) {
}
