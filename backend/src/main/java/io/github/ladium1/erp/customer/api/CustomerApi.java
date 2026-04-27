package io.github.ladium1.erp.customer.api;

import io.github.ladium1.erp.customer.api.dto.CustomerInfo;
import io.github.ladium1.erp.customer.api.dto.RecentCustomerInfo;

import java.util.List;

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
}
