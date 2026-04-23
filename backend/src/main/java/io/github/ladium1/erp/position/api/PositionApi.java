package io.github.ladium1.erp.position.api;

import io.github.ladium1.erp.position.api.dto.PositionInfo;

import java.util.List;

public interface PositionApi {
    /**
     * 직책 정보 반환
     */
    PositionInfo getById(Long id);

    /**
     * 전체 직책 목록 반환 (이름 오름차순)
     */
    List<PositionInfo> findAll();

    /**
     * 주어진 id 목록에 해당하는 직책 정보 반환
     */
    List<PositionInfo> findByIds(List<Long> ids);
}
