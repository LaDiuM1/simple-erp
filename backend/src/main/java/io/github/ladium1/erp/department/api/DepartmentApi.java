package io.github.ladium1.erp.department.api;

import io.github.ladium1.erp.department.api.dto.DepartmentInfo;

public interface DepartmentApi {
    /**
     * 부서 정보 반환
     */
    DepartmentInfo getById(Long id);
}