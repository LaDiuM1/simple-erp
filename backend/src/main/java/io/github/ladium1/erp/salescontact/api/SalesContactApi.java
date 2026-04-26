package io.github.ladium1.erp.salescontact.api;

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
}
