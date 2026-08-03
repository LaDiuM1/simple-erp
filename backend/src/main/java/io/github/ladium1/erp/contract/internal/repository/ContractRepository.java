package io.github.ladium1.erp.contract.internal.repository;

import io.github.ladium1.erp.contract.internal.entity.Contract;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ContractRepository extends JpaRepository<Contract, Long>, ContractRepositoryCustom {

    boolean existsByContractNo(String contractNo);

    /** 설치 전이·삭제를 직렬화해 비동기 설비 생성과 계약 snapshot의 기준 행을 보호한다. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from Contract c where c.id = :id")
    Optional<Contract> findByIdForUpdate(@Param("id") Long id);

    boolean existsByCustomerId(Long customerId);

    boolean existsByProductId(Long productId);

    boolean existsBySupplierId(Long supplierId);
}
