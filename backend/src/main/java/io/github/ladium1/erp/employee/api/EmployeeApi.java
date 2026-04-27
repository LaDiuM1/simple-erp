package io.github.ladium1.erp.employee.api;

import io.github.ladium1.erp.employee.api.dto.EmployeeInfo;

import java.util.List;
import java.util.Optional;

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
     * 로그인 ID로 직원 정보 조회 — 없으면 Optional.empty (시드 / 외부 모듈 lookup 용)
     */
    Optional<EmployeeInfo> findByLoginId(String loginId);

    /**
     * 주어진 id 목록에 해당하는 직원 정보 반환
     */
    List<EmployeeInfo> findByIds(List<Long> ids);

    /**
     * 재직 중 직원 수 (퇴사자 제외) — 대시보드 KPI 용.
     */
    long countActive();
}
