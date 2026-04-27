package io.github.ladium1.erp.salescustomer.api.dto;

import lombok.Builder;

import java.time.LocalDateTime;

/**
 * 대시보드 / 모듈 외부에서 소비하는 최근 영업 활동 정보. 활동 유형은 enum name() 그대로 노출.
 */
@Builder
public record RecentSalesActivityInfo(
        Long id,
        Long customerId,
        String customerCode,
        String customerName,
        String type,
        String subject,
        LocalDateTime activityDate,
        Long ourEmployeeId,
        String ourEmployeeName
) {
}
