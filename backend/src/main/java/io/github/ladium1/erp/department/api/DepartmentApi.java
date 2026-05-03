package io.github.ladium1.erp.department.api;

import io.github.ladium1.erp.department.api.dto.DepartmentInfo;

import java.util.List;
import java.util.Set;

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

    /**
     * 주어진 부서 + 모든 하위 부서의 식별자 집합 반환 (DEPARTMENT_TREE 스코프 등에서 사용).
     * rootId 자체도 결과에 포함. rootId 가 null 이면 빈 집합.
     */
    Set<Long> findSubtreeIds(Long rootId);
}
