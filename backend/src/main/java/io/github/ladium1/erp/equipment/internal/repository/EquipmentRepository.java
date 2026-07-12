package io.github.ladium1.erp.equipment.internal.repository;

import io.github.ladium1.erp.equipment.internal.entity.Equipment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EquipmentRepository extends JpaRepository<Equipment, Long>, EquipmentRepositoryCustom {

    boolean existsByContractId(Long contractId);

    boolean existsByCustomerId(Long customerId);

    boolean existsByProductId(Long productId);

    boolean existsBySupplierId(Long supplierId);
}
