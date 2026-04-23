package io.github.ladium1.erp.member.internal.dto;

import io.github.ladium1.erp.member.internal.entity.MemberStatus;
import lombok.Builder;

import java.time.LocalDate;

@Builder
public record MemberDetailResponse(
        Long id,
        String loginId,
        String name,
        String email,
        String phone,
        String zipCode,
        String roadAddress,
        String detailAddress,
        LocalDate joinDate,
        MemberStatus status,
        Long departmentId,
        String departmentName,
        Long positionId,
        String positionName,
        Long roleId,
        String roleName
) {
}
