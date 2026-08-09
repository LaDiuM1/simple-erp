package io.github.ladium1.erp.employee.api;

import io.github.ladium1.erp.employee.api.dto.EmployeeInfo;

import java.util.Collection;
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
    long countCurrentlyEmployed();

    /**
     * 재직 중 직원 전체 (퇴사자 제외) — 관리자 화면의 전 직원 벌크 조회 용.
     */
    List<EmployeeInfo> findAllCurrentlyEmployed();

    /**
     * 직원이 현재 재직 관계에 있는지 판정한다. 휴직은 재직에 포함하고 퇴사는 제외한다.
     */
    boolean isCurrentlyEmployed(Long employeeId);

    /**
     * 신규 업무가 단일 직원을 참조할 수 있는지 판정한다.
     */
    boolean isEligibleForNewWorkReference(Long employeeId);

    /**
     * 신규 업무의 담당자, 결재자, 내부 엔지니어처럼 새 참조를 만들 수 있는 직원인지 일괄 판정한다.
     * 모든 ID가 존재하고 ACTIVE 상태이며 복구 전용 운영 계정이 아니어야 한다.
     */
    boolean allEligibleForNewWorkReference(Collection<Long> employeeIds);

    /**
     * 주어진 부서들에 속한 직원 식별자 목록 — 데이터 스코프 (DEPARTMENT / DEPARTMENT_TREE) 적용 시 사용.
     * 빈 입력은 빈 리스트 반환.
     */
    List<Long> findIdsByDepartmentIds(Collection<Long> departmentIds);
}
