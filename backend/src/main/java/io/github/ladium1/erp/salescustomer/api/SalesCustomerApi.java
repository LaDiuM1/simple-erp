package io.github.ladium1.erp.salescustomer.api;

import io.github.ladium1.erp.salescustomer.api.dto.FollowUpCustomerInfo;
import io.github.ladium1.erp.salescustomer.api.dto.RecentSalesActivityInfo;
import io.github.ladium1.erp.salescustomer.api.dto.WeeklyActivityCountInfo;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

public interface SalesCustomerApi {

    /**
     * 주어진 시점 이후의 영업 활동 수 — 대시보드의 "이번 달 영업 활동" KPI 등에 사용.
     */
    long countActivitiesSince(LocalDateTime since);

    /**
     * 최근 영업 활동 N건 (activityDate 내림차순) — 고객사명 / 담당 직원명 enrich.
     */
    List<RecentSalesActivityInfo> findRecentActivities(int limit);

    /**
     * 최근 N주의 주 단위 활동 집계 (오래된 주 -> 이번 주 순, 활동 없는 주는 0 채움) — 대시보드 추이 차트용.
     */
    List<WeeklyActivityCountInfo> countActivitiesByWeek(int weeks);

    /**
     * 주어진 시점 이후 활동이 1건 이상 있는 고객사 수 (중복 제거) — 대시보드 미접촉 고객 산출용.
     */
    long countDistinctCustomersWithActivitySince(LocalDateTime since);

    /**
     * 팔로업 필요 고객 — 활성 담당이 배정된 고객사 중 마지막 활동이 staleDays 초과 경과했거나
     * 활동이 없는 고객을 오래된 순으로 최대 limit 건. visibleCustomerIds 가 null 이면 제한 없음(ALL),
     * 값이 있으면 해당 고객사로 한정 (데이터 스코프).
     */
    List<FollowUpCustomerInfo> findFollowUpTargets(int staleDays, int limit, Set<Long> visibleCustomerIds);
}
