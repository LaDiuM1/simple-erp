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
}
