package io.github.ladium1.erp.salescustomer.internal.dto;

import lombok.Builder;

import java.time.LocalDateTime;

/**
 * 영업 관리 목록 보강용 — 고객사별 활성 담당자 / 활동 집계.
 * customer 마스터 페이지는 customer 모듈에서 별도 조회, 이 집계는 customerIds 로 매핑.
 */
@Builder
public record SalesCustomerAggregate(
        Long customerId,
        Long primaryAssigneeId,
        String primaryAssigneeName,
        long activeAssigneeCount,
        long activityCount,
        LocalDateTime lastActivityDate
) {
}
