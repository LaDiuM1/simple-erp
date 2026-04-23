package io.github.ladium1.erp.member.internal.dto;

import io.github.ladium1.erp.member.internal.entity.MemberStatus;
import lombok.Builder;

import java.time.LocalDate;

@Builder
public record MemberSummaryResponse(
        Long id,
        String loginId,
        String name,
        String departmentName,
        String positionName,
        String roleName,
        String email,
        String phone,
        LocalDate joinDate,
        MemberStatus status
) {
}
