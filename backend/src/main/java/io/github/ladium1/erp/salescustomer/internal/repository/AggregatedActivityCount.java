package io.github.ladium1.erp.salescustomer.internal.repository;

import java.time.LocalDateTime;

/**
 * 활동 카운트 + 마지막 활동일 집계용 record. JPQL constructor expression 으로 생성.
 */
public record AggregatedActivityCount(Long customerId, long count, LocalDateTime lastActivityDate) {
}
