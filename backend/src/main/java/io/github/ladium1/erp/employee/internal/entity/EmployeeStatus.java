package io.github.ladium1.erp.employee.internal.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum EmployeeStatus {

    ACTIVE("재직"),
    LEAVE("휴직"),
    RESIGNED("퇴사");

    private final String description;
}
