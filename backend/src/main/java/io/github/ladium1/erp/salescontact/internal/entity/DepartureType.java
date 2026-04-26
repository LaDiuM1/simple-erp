package io.github.ladium1.erp.salescontact.internal.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum DepartureType {

    JOB_CHANGE("이직"),
    RETIREMENT("퇴직"),
    OTHER("기타");

    private final String description;
}
