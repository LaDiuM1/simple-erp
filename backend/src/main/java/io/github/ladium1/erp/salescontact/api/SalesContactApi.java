package io.github.ladium1.erp.salescontact.api;

import io.github.ladium1.erp.salescontact.api.dto.RecentSalesContactInfo;
import io.github.ladium1.erp.salescontact.api.dto.SalesContactInfo;

import java.util.List;

public interface SalesContactApi {

    /**
     * 영업 명부 정보 반환 (활성 재직 회사명 / 직책 포함)
     */
    SalesContactInfo getById(Long id);

    /**
     * 주어진 id 목록에 해당하는 영업 명부 정보 반환
     */
    List<SalesContactInfo> findByIds(List<Long> ids);

    /**
     * 명함이 현재 해당 고객사에 재직 중인지 확인한다. 종료된 이력과 외부 회사 이력은 제외한다.
     */
    boolean hasActiveEmploymentAtCustomer(Long contactId, Long customerId);

    /**
     * 전체 영업 명부 수 — 대시보드 KPI 용.
     */
    long count();

    /**
     * 최근 등록된 영업 명부 N건 (createdAt 내림차순) — 대시보드 용.
     */
    List<RecentSalesContactInfo> findRecent(int limit);
}
