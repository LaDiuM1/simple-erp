package io.github.ladium1.erp.contract.internal.repository;

import io.github.ladium1.erp.contract.internal.entity.ContractPayment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ContractPaymentRepository extends JpaRepository<ContractPayment, Long>, ContractPaymentRepositoryCustom {

    /** 등록 순서 = 회차 순서 (계약금 → 중도금 → 잔금 순으로 입력하는 실무 관행) */
    List<ContractPayment> findByContractIdOrderByIdAsc(Long contractId);

    void deleteByContractId(Long contractId);
}
