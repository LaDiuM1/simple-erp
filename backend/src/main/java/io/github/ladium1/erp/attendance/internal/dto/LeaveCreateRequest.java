package io.github.ladium1.erp.attendance.internal.dto;

import io.github.ladium1.erp.attendance.internal.entity.LeaveType;
import io.github.ladium1.erp.global.validation.RequestCollectionPolicy;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.List;

public record LeaveCreateRequest(
        @NotNull LeaveType leaveType,
        @NotNull LocalDate startDate,
        @NotNull LocalDate endDate,
        @Size(max = 500) String reason,
        @NotEmpty @Size(max = RequestCollectionPolicy.MAX_MUTATION_BATCH_SIZE)
        List<@NotNull Long> approverIds
) {
}
