package io.github.ladium1.erp.equipment.api;

import io.github.ladium1.erp.equipment.api.dto.EquipmentInfo;

import java.util.List;

public interface EquipmentApi {

    /**
     * 설비 정보 반환 (보증 만료일 포함) — AS 접수의 참조 검증 / 유상·무상 판정용
     */
    EquipmentInfo getById(Long id);

    /**
     * 주어진 id 목록에 해당하는 설비 정보 반환
     */
    List<EquipmentInfo> findByIds(List<Long> ids);
}
