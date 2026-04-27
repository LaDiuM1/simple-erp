package io.github.ladium1.erp.salescustomer.api;

import io.github.ladium1.erp.salescustomer.api.dto.RecentSalesActivityInfo;

import java.time.LocalDateTime;
import java.util.List;

public interface SalesCustomerApi {

    /**
     * 주어진 시점 이후의 영업 활동 수 — 대시보드의 "이번 달 영업 활동" KPI 등에 사용.
     */
    long countActivitiesSince(LocalDateTime since);

    /**
     * 최근 영업 활동 N건 (activityDate 내림차순) — 고객사명 / 담당 직원명 enrich.
     */
    List<RecentSalesActivityInfo> findRecentActivities(int limit);
}
