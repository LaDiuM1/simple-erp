package io.github.ladium1.erp.attendance.internal.repository;

import io.github.ladium1.erp.attendance.internal.entity.LeaveBalance;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import java.util.List;
import java.util.Optional;

public interface LeaveBalanceRepository extends JpaRepository<LeaveBalance, Long> {

    Optional<LeaveBalance> findByEmployeeIdAndYear(Long employeeId, int year);

    /**
     * 잠금 (PESSIMISTIC_WRITE) 조회 — 승인 콜백의 잔여 재검증 + 차감이 동시 승인과 직렬화되도록.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<LeaveBalance> findWithLockByEmployeeIdAndYear(Long employeeId, int year);

    List<LeaveBalance> findAllByYear(int year);
}
