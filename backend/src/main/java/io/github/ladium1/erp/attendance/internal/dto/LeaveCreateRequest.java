package io.github.ladium1.erp.attendance.internal.dto;

import io.github.ladium1.erp.attendance.internal.entity.LeaveType;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.List;

public record LeaveCreateRequest(
        @NotNull LeaveType leaveType,
        @NotNull LocalDate startDate,
        @NotNull LocalDate endDate,
        String reason,
        @NotEmpty List<Long> approverIds
) {
}
