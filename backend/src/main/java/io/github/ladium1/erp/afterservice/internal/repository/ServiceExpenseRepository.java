package io.github.ladium1.erp.afterservice.internal.repository;

import io.github.ladium1.erp.afterservice.internal.entity.ServiceExpense;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ServiceExpenseRepository extends JpaRepository<ServiceExpense, Long>, ServiceExpenseRepositoryCustom {

    /** 등록 순서 유지 — 경비는 방문 흐름 순으로 입력하는 실무 관행 */
    List<ServiceExpense> findByAfterServiceIdOrderByIdAsc(Long afterServiceId);

    void deleteByAfterServiceId(Long afterServiceId);

    boolean existsByEngineerId(Long engineerId);
}
