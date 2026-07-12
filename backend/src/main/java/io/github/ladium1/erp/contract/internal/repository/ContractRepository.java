package io.github.ladium1.erp.contract.internal.repository;

import io.github.ladium1.erp.contract.internal.entity.Contract;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ContractRepository extends JpaRepository<Contract, Long>, ContractRepositoryCustom {

    boolean existsByContractNo(String contractNo);

    boolean existsByCustomerId(Long customerId);

    boolean existsByProductId(Long productId);

    boolean existsBySupplierId(Long supplierId);
}
