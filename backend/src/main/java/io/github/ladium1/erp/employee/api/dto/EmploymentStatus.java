package io.github.ladium1.erp.employee.api.dto;

/**
 * 직원 모듈 밖에서 신규 업무 배정 가능 여부를 판단할 때 사용하는 재직 상태 계약.
 */
public enum EmploymentStatus {
    ACTIVE,
    LEAVE,
    RESIGNED
}
