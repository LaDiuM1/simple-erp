package io.github.ladium1.erp.contract.api;

import io.github.ladium1.erp.contract.api.dto.ContractInfo;

import java.util.List;

public interface ContractApi {

    /**
     * 계약 정보 반환
     */
    ContractInfo getById(Long id);

    /**
     * 주어진 id 목록에 해당하는 계약 정보 반환 — 설비 대장 등의 계약번호 표시용
     */
    List<ContractInfo> findByIds(List<Long> ids);
}
