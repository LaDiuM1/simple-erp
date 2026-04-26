package io.github.ladium1.erp.salescontact.internal.repository;

import io.github.ladium1.erp.salescontact.internal.entity.SalesContactEmployment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface SalesContactEmploymentRepository extends JpaRepository<SalesContactEmployment, Long> {

    /**
     * 명부의 전체 재직 이력 — 활성(endDate=null)이 먼저, 그다음 startDate 내림차순.
     */
    List<SalesContactEmployment> findByContactIdOrderByEndDateAscStartDateDesc(Long contactId);

    /**
     * 고객사별 재직 이력 — 활성 + 종료된 이력 모두, endDate 오름차순(null 먼저) + startDate 내림차순.
     */
    List<SalesContactEmployment> findByCustomerIdOrderByEndDateAscStartDateDesc(Long customerId);

    /**
     * 명부 ids 의 활성 재직 — 목록 보강용 (현재 회사 / 직책 매핑).
     */
    List<SalesContactEmployment> findByContactIdInAndEndDateIsNull(Collection<Long> contactIds);

    /**
     * 명부의 활성 재직 — 동시 다중 재직 허용 / 단건 조회 시 첫 번째.
     */
    List<SalesContactEmployment> findByContactIdAndEndDateIsNull(Long contactId);
}
