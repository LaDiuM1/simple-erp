package io.github.ladium1.erp.attendance.internal.dto;

import io.github.ladium1.erp.attendance.internal.entity.LeaveRequest;
import io.github.ladium1.erp.attendance.internal.entity.LeaveStatus;
import io.github.ladium1.erp.attendance.internal.entity.LeaveType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record LeaveAdminResponse(
        Long id,
        Long employeeId,
        String employeeName,
        LeaveType leaveType,
        LocalDate startDate,
        LocalDate endDate,
        BigDecimal days,
        String reason,
        LeaveStatus status,
        Long approvalDocumentId,
        LocalDateTime createdAt
) {

    public static LeaveAdminResponse from(LeaveRequest leaveRequest, String employeeName) {
        return new LeaveAdminResponse(
                leaveRequest.getId(),
                leaveRequest.getEmployeeId(),
                employeeName,
                leaveRequest.getLeaveType(),
                leaveRequest.getStartDate(),
                leaveRequest.getEndDate(),
                leaveRequest.getDays(),
                leaveRequest.getReason(),
                leaveRequest.getStatus(),
                leaveRequest.getApprovalDocumentId(),
                leaveRequest.getCreatedAt()
        );
    }
}
