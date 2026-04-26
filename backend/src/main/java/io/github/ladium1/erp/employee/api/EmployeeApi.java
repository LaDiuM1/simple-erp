package io.github.ladium1.erp.employee.api;

import io.github.ladium1.erp.employee.api.dto.EmployeeInfo;

import java.util.List;

public interface EmployeeApi {

    /**
     * 로그인 ID로 직원의 권한(역할) 식별자 반환
     */
    Long getRoleIdByLoginId(String loginId);

    /**
     * 직원 정보 반환 (부서명 / 직책명 포함)
     */
    EmployeeInfo getById(Long id);

    /**
     * 주어진 id 목록에 해당하는 직원 정보 반환
     */
    List<EmployeeInfo> findByIds(List<Long> ids);
}
