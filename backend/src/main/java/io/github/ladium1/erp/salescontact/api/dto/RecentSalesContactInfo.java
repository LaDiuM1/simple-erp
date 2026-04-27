package io.github.ladium1.erp.salescontact.api.dto;

import lombok.Builder;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 대시보드 / 모듈 외부에서 소비하는 최근 영업 명부 정보. 활성 재직 회사명·직책 캐시 포함.
 */
@Builder
public record RecentSalesContactInfo(
        Long id,
        String name,
        String currentCompanyName,
        String currentPosition,
        LocalDate metAt,
        LocalDateTime createdAt
) {
}
