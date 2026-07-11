package io.github.ladium1.erp.contract.internal.repository;

import io.github.ladium1.erp.contract.internal.entity.ContractNote;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ContractNoteRepository extends JpaRepository<ContractNote, Long> {

    /** 최신 메모 먼저 — 이력 조회 관점 */
    List<ContractNote> findByContractIdOrderByIdDesc(Long contractId);

    void deleteByContractId(Long contractId);
}
