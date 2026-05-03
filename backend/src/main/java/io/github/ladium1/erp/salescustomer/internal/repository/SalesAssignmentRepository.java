package io.github.ladium1.erp.salescustomer.internal.repository;

import io.github.ladium1.erp.salescustomer.internal.entity.SalesAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.List;

public interface SalesAssignmentRepository extends JpaRepository<SalesAssignment, Long> {

    /**
     * 고객사의 모든 배정 (활성 + 종료) — endDate ASC (MariaDB 의 ASC NULLS FIRST 기본 동작에 의존하여 활성 = null 이 먼저), 그다음 startDate DESC.
     */
    List<SalesAssignment> findByCustomerIdOrderByEndDateAscStartDateDesc(Long customerId);

    /**
     * 고객사들의 활성 배정 (endDate is null) — 목록 보강용.
     */
    List<SalesAssignment> findByCustomerIdInAndEndDateIsNull(Collection<Long> customerIds);

    /**
     * 한 고객사에 현재 주담당이 있는지 — primary 변경 시 기존 primary 해제용.
     */
    List<SalesAssignment> findByCustomerIdAndPrimaryTrueAndEndDateIsNull(Long customerId);

    boolean existsByEmployeeIdAndEndDateIsNull(Long employeeId);

    /**
     * 주어진 직원들 중 누군가가 활성 담당으로 배정된 고객사 식별자 목록 (중복 제거).
     * 데이터 스코프 (SELF / DEPARTMENT(_TREE)) 적용 시 customer 가시성 결정용.
     */
    @Query("select distinct a.customerId from SalesAssignment a where a.endDate is null and a.employeeId in :employeeIds")
    List<Long> findActiveCustomerIdsByEmployeeIds(Collection<Long> employeeIds);
}
