package io.github.ladium1.erp.salescustomer.internal.repository;

import io.github.ladium1.erp.salescustomer.internal.entity.SalesActivity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

public interface SalesActivityRepository extends JpaRepository<SalesActivity, Long> {

    List<SalesActivity> findByCustomerIdOrderByActivityDateDesc(Long customerId);

    List<SalesActivity> findByCustomerContactIdOrderByActivityDateDesc(Long customerContactId);

    long countByActivityDateGreaterThanEqual(LocalDateTime since);

    List<SalesActivity> findAllByOrderByActivityDateDesc(Pageable pageable);

    /**
     * 여러 고객사의 활동 카운트를 한 번에 집계 — 목록 페이지 보강용.
     */
    @Query("""
            select new io.github.ladium1.erp.salescustomer.internal.repository.AggregatedActivityCount(
                a.customerId, count(a), max(a.activityDate)
            )
            from SalesActivity a
            where a.customerId in :customerIds
            group by a.customerId
            """)
    List<AggregatedActivityCount> aggregateByCustomerIds(@Param("customerIds") Collection<Long> customerIds);
}
