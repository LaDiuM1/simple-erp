package io.github.ladium1.erp.customer.api.dto;

import lombok.Builder;

import java.time.LocalDateTime;

/**
 * 대시보드 / 모듈 외부에서 소비하는 최근 고객사 정보. 분류·상태는 enum name() 그대로 노출 — 호출 측이 라벨/색상 매핑.
 */
@Builder
public record RecentCustomerInfo(
        Long id,
        String code,
        String name,
        String type,
        String status,
        LocalDateTime createdAt
) {
}
