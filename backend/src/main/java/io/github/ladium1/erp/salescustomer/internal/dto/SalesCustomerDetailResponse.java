package io.github.ladium1.erp.salescustomer.internal.dto;

import lombok.Builder;

import java.util.List;

/**
 * 영업 관리 상세 응답 — 고객사 식별 정보 + 활동 + 담당자(현재/이력) 통합.
 */
@Builder
public record SalesCustomerDetailResponse(
        Long customerId,
        String customerCode,
        String customerName,
        List<SalesActivityResponse> activities,
        List<SalesAssignmentResponse> assignments
) {
}
