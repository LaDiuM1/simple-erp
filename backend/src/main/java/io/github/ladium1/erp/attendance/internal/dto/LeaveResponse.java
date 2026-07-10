package io.github.ladium1.erp.attendance.internal.dto;

import io.github.ladium1.erp.attendance.internal.entity.LeaveRequest;
import io.github.ladium1.erp.attendance.internal.entity.LeaveStatus;
import io.github.ladium1.erp.attendance.internal.entity.LeaveType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record LeaveResponse(
        Long id,
        LeaveType leaveType,
        LocalDate startDate,
        LocalDate endDate,
        BigDecimal days,
        String reason,
        LeaveStatus status,
        Long approvalDocumentId,
        LocalDateTime createdAt
) {

    public static LeaveResponse from(LeaveRequest leaveRequest) {
        return new LeaveResponse(
                leaveRequest.getId(),
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
