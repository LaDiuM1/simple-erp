package io.github.ladium1.erp.afterservice.internal.repository;

import io.github.ladium1.erp.afterservice.internal.entity.ServiceVisit;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ServiceVisitRepository extends JpaRepository<ServiceVisit, Long> {

    /** 최근 방문 먼저 — 일지 조회 관점 */
    List<ServiceVisit> findByAfterServiceIdOrderByVisitDateDescIdDesc(Long afterServiceId);

    void deleteByAfterServiceId(Long afterServiceId);

    boolean existsByEngineerId(Long engineerId);
}
