package io.github.ladium1.erp.equipment.internal.repository;

import io.github.ladium1.erp.equipment.internal.dto.EquipmentSearchCondition;
import io.github.ladium1.erp.equipment.internal.entity.Equipment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.List;

public interface EquipmentRepositoryCustom {

    Page<Equipment> search(EquipmentSearchCondition condition, Pageable pageable);

    List<Equipment> searchAll(EquipmentSearchCondition condition, Sort sort);

    /**
     * 발진기 / 무상 AS 중 하나라도 [오늘, 오늘+days] 에 만료되는 설비 — 무상 AS 만료일 오름차순, 최대 limit 건.
     */
    List<Equipment> findExpiringWarranties(int days, int limit);
}
