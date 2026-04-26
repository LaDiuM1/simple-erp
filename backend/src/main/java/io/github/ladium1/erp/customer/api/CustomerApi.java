package io.github.ladium1.erp.customer.api;

import io.github.ladium1.erp.customer.api.dto.CustomerInfo;

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
}
