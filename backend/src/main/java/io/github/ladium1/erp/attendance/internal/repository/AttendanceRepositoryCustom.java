package io.github.ladium1.erp.attendance.internal.repository;

import io.github.ladium1.erp.attendance.internal.dto.AttendanceSearchCondition;
import io.github.ladium1.erp.attendance.internal.entity.Attendance;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AttendanceRepositoryCustom {

    Page<Attendance> search(AttendanceSearchCondition condition, Pageable pageable);
}
