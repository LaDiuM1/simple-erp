package io.github.ladium1.erp.member.internal.dto;

import io.github.ladium1.erp.member.internal.entity.MemberStatus;

public record MemberSearchCondition(
        String keyword,
        Long departmentId,
        Long positionId,
        Long roleId,
        MemberStatus status
) {
}
