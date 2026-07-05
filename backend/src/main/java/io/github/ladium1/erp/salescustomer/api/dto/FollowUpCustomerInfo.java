package io.github.ladium1.erp.salescustomer.api.dto;

import lombok.Builder;

import java.time.LocalDateTime;

/**
 * 팔로업 필요 고객 — 활성 담당이 배정된 고객사 중 마지막 활동이 기준일을 초과해 경과했거나
 * 활동이 아예 없는 고객. lastActivityDate 가 null 이면 활동 기록 없음.
 */
@Builder
public record FollowUpCustomerInfo(
        Long customerId,
        String customerCode,
        String customerName,
        LocalDateTime lastActivityDate,
        Long primaryAssigneeId,
        String primaryAssigneeName
) {
}
