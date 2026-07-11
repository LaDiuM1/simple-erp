package io.github.ladium1.erp.attendance.internal.repository;

import io.github.ladium1.erp.attendance.internal.entity.Attendance;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface AttendanceRepository extends JpaRepository<Attendance, Long>, AttendanceRepositoryCustom {

    Optional<Attendance> findByEmployeeIdAndWorkDate(Long employeeId, LocalDate workDate);

    /** 자정 넘긴 퇴근 fallback — 해당 근무일의 미퇴근 행 조회. */
    Optional<Attendance> findByEmployeeIdAndWorkDateAndCheckOutAtIsNull(Long employeeId, LocalDate workDate);

    List<Attendance> findByEmployeeIdAndWorkDateBetweenOrderByWorkDateAsc(Long employeeId, LocalDate start, LocalDate end);
}
