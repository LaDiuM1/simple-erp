package io.github.ladium1.erp.global.security;

public record DataScopePrincipal(
        Long employeeId,
        Long roleId,
        Long departmentId
) {
}
