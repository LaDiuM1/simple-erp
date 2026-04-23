package io.github.ladium1.erp.member.internal.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum MemberStatus {

    ACTIVE("재직"),
    LEAVE("휴직"),
    RESIGNED("퇴사");

    private final String description;
}
