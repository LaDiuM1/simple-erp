package io.github.ladium1.erp.afterservice.internal.repository;

import io.github.ladium1.erp.afterservice.internal.entity.AfterService;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AfterServiceRepository extends JpaRepository<AfterService, Long>, AfterServiceRepositoryCustom {

    boolean existsByReceiptNo(String receiptNo);

    boolean existsByCustomerId(Long customerId);

    boolean existsByEquipmentId(Long equipmentId);

    boolean existsByAssignedEngineerId(Long engineerId);
}
