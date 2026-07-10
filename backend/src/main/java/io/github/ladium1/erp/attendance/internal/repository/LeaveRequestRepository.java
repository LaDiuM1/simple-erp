package io.github.ladium1.erp.attendance.internal.repository;

import io.github.ladium1.erp.attendance.internal.entity.LeaveRequest;
import io.github.ladium1.erp.attendance.internal.entity.LeaveType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, Long>, LeaveRequestRepositoryCustom {

    List<LeaveRequest> findByEmployeeIdOrderByIdDesc(Long employeeId);

    /** 같은 직원의 결재 중 / 승인 상태 신청과 기간이 겹치는 건 존재 여부 — 중복 신청 차단 용. */
    @Query("""
           select count(l) > 0 from LeaveRequest l
           where l.employeeId = :employeeId
           and l.status in (LeaveStatus.IN_PROGRESS, LeaveStatus.APPROVED)
           and l.startDate <= :endDate
           and l.endDate >= :startDate
           """)
    boolean existsOverlappingPeriod(Long employeeId, LocalDate startDate, LocalDate endDate);

    /** 본인의 결재 중 (IN_PROGRESS) 신청 일수 합 — 신청 시점 잔여 검증에 선반영. 연도는 시작일 기준. */
    @Query("""
           select coalesce(sum(l.days), 0) from LeaveRequest l
           where l.employeeId = :employeeId
           and l.status = LeaveStatus.IN_PROGRESS
           and l.leaveType in :leaveTypes
           and year(l.startDate) = :year
           """)
    BigDecimal sumInProgressDays(Long employeeId, Collection<LeaveType> leaveTypes, int year);
}
