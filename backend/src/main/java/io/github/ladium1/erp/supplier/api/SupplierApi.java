package io.github.ladium1.erp.supplier.api;

import io.github.ladium1.erp.supplier.api.dto.SupplierInfo;

import java.util.List;

public interface SupplierApi {
    /**
     * 공급사 정보 반환
     */
    SupplierInfo getById(Long id);

    /**
     * 전체 공급사 목록 반환 (이름 오름차순)
     */
    List<SupplierInfo> findAll();

    /**
     * 주어진 id 목록에 해당하는 공급사 정보 반환
     */
    List<SupplierInfo> findByIds(List<Long> ids);
}
