package io.github.ladium1.erp.department.api;

import io.github.ladium1.erp.department.api.dto.DepartmentInfo;

import java.util.List;

public interface DepartmentApi {
    /**
     * 부서 정보 반환
     */
    DepartmentInfo getById(Long id);

    /**
     * 전체 부서 목록 반환 (이름 오름차순)
     */
    List<DepartmentInfo> findAll();

    /**
     * 주어진 id 목록에 해당하는 부서 정보 반환
     */
    List<DepartmentInfo> findByIds(List<Long> ids);
}
