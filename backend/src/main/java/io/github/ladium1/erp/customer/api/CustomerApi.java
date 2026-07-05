package io.github.ladium1.erp.customer.api;

import io.github.ladium1.erp.customer.api.dto.CustomerInfo;
import io.github.ladium1.erp.customer.api.dto.RecentCustomerInfo;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface CustomerApi {
    /**
     * 고객사 정보 반환
     */
    CustomerInfo getById(Long id);

    /**
     * 전체 고객사 목록 반환 (이름 오름차순)
     */
    List<CustomerInfo> findAll();

    /**
     * 주어진 id 목록에 해당하는 고객사 정보 반환
     */
    List<CustomerInfo> findByIds(List<Long> ids);

    /**
     * 전체 고객사 수 — 대시보드 KPI 용.
     */
    long count();

    /**
     * 최근 등록된 고객사 N건 (createdAt 내림차순) — 대시보드 용.
     */
    List<RecentCustomerInfo> findRecent(int limit);

    /**
     * 주어진 시점 이후 등록된 고객사 수 — 대시보드 기간 증감용.
     */
    long countCreatedSince(LocalDateTime since);

    /**
     * 현재 사용자의 데이터 스코프 기준 가시 고객사 ID 집합 — Optional.empty() 는 ALL (제한 없음).
     * 대시보드 등 외부 모듈이 고객 파생 데이터에 동일한 스코프를 적용할 때 사용.
     */
    Optional<Set<Long>> resolveVisibleCustomerIds();
}
