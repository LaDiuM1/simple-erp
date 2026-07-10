package io.github.ladium1.erp.attendance.internal.dto;

import io.github.ladium1.erp.attendance.internal.entity.Attendance;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record AttendanceResponse(
        Long id,
        Long employeeId,
        String employeeName,
        LocalDate workDate,
        LocalDateTime checkInAt,
        LocalDateTime checkOutAt,
        boolean checkInWithinRange,
        boolean checkOutWithinRange
) {

    public static AttendanceResponse from(Attendance attendance, String employeeName) {
        return new AttendanceResponse(
                attendance.getId(),
                attendance.getEmployeeId(),
                employeeName,
                attendance.getWorkDate(),
                attendance.getCheckInAt(),
                attendance.getCheckOutAt(),
                attendance.isCheckInWithinRange(),
                attendance.isCheckOutWithinRange()
        );
    }
}
