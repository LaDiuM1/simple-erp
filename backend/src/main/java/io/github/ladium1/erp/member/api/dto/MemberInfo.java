package io.github.ladium1.erp.member.api.dto;

import lombok.Builder;

@Builder
public record MemberInfo(
        Long id,
        String loginId,
        String name,
        String departmentName,
        String positionName
) {
}
