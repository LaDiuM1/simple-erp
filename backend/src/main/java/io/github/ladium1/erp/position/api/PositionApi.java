package io.github.ladium1.erp.position.api;

import io.github.ladium1.erp.position.api.dto.PositionInfo;

public interface PositionApi {
    /**
     * 직책 정보 반환
     */
    PositionInfo getById(Long id);
}