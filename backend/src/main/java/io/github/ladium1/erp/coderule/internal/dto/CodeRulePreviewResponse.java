package io.github.ladium1.erp.coderule.internal.dto;

import lombok.Builder;

import java.util.List;

/**
 * 미리보기 결과.
 *
 * @param nextCode 현재 시퀀스 +1 로 생성될 코드
 * @param samples  시퀀스를 1, 2, 3, ... 으로 증가시켜 본 샘플 코드 목록 (UI 시뮬레이션 용)
 */
@Builder
public record CodeRulePreviewResponse(
        String nextCode,
        List<String> samples
) {
}
