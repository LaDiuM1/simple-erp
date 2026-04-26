package io.github.ladium1.erp.position.internal.dto;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;

/**
 * 서열 일괄 재배치 요청.
 * <p>
 * 상위 → 하위 순서로 정렬된 직책 ID 배열. 서버는 1, 2, 3, ... 으로 일괄 갱신한다.
 * 누락된 직책이 있거나 알 수 없는 ID 가 섞여 있으면 BusinessException 으로 거부.
 */
public record PositionRankingRequest(
        @NotEmpty
        List<Long> orderedIds
) {
}
