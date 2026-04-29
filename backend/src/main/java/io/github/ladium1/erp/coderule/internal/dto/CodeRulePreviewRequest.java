package io.github.ladium1.erp.coderule.internal.dto;

import io.github.ladium1.erp.coderule.api.InputMode;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.Map;

/**
 * 저장 전 미리보기용 — 사용자가 편집 중인 패턴으로 다음 코드를 시뮬레이션.
 * <p>
 * 시퀀스는 현재 저장된 값을 기준으로 +1 한 결과를 반환하고 실제 증가는 일어나지 않는다.
 */
public record CodeRulePreviewRequest(
        @NotBlank @Size(max = 200)
        String pattern,

        @NotNull
        InputMode inputMode,

        String parentCode,

        /** 미리보기 시 분류 토큰 치환에 사용할 sourceValue 들 (사용자가 모달에서 선택) */
        Map<String, String> previewAttributes,

        /** 폼에서 편집 중인 매핑 — DB 저장된 매핑 대신 이 값을 사용해 정확한 미리보기 시뮬레이션 */
        @Valid
        List<CodeRuleAttributeMappingPayload> attributeMappings
) {
}
