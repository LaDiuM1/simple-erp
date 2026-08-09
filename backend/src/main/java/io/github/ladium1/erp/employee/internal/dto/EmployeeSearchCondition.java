package io.github.ladium1.erp.employee.internal.dto;

import io.github.ladium1.erp.employee.internal.entity.EmployeeStatus;

public record EmployeeSearchCondition(
        String loginIdKeyword,
        String nameKeyword,
        Long departmentId,
        Long positionId,
        Long roleId,
        EmployeeStatus status,
        String excludedLoginId
) {

    public EmployeeSearchCondition(String loginIdKeyword, String nameKeyword, Long departmentId,
                                   Long positionId, Long roleId, EmployeeStatus status) {
        this(loginIdKeyword, nameKeyword, departmentId, positionId, roleId, status, null);
    }

    public EmployeeSearchCondition withExcludedLoginId(String loginId) {
        return new EmployeeSearchCondition(loginIdKeyword, nameKeyword, departmentId,
                positionId, roleId, status, loginId);
    }
}
