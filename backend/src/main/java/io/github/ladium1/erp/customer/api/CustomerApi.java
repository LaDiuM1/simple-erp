package io.github.ladium1.erp.customer.api;

import io.github.ladium1.erp.customer.api.dto.CustomerInfo;
import io.github.ladium1.erp.customer.api.dto.RecentCustomerInfo;
import io.github.ladium1.erp.global.menu.Menu;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface CustomerApi {
    /**
     * 고객사 정보 반환
     */
    CustomerInfo getById(Long id);

    /**
     * 지정 메뉴의 데이터 범위에서 고객사가 보이지 않으면 존재 여부와 무관하게 NOT_FOUND로 처리한다.
     */
    void assertVisibleToCurrentViewer(Menu menu, Long id);

    /**
     * 입력 순서를 유지하면서 지정 메뉴의 데이터 범위에 보이는 고객사 ID만 반환한다.
     */
    List<Long> filterVisibleIdsForCurrentViewer(Menu menu, Collection<Long> ids);

    /**
     * 지정 메뉴에 대한 현재 조회자의 고객사 ID 제한.
     */
    Optional<Set<Long>> currentViewerIdRestriction(Menu menu);

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
    long countVisibleToCurrentViewer();

    /**
     * 최근 등록된 고객사 N건 (createdAt 내림차순) — 대시보드 용.
     */
    List<RecentCustomerInfo> findRecentVisibleToCurrentViewer(int limit);
}
