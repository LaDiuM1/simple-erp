package io.github.ladium1.erp.afterservice.internal.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * AS 건 진행 상태.
 */
@Getter
@RequiredArgsConstructor
public enum ServiceStatus {

    RECEIVED("접수"),
    ASSIGNED("배정"),
    IN_PROGRESS("진행중"),
    COMPLETED("완료");

    private final String description;
}
