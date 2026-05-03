package io.github.ladium1.erp.global.security;

import java.util.Set;

/**
 * 데이터 스코프 평가에 필요한 현재 사용자 정보 스냅샷.
 *
 * <p>가시성 contributor 가 SELF / DEPARTMENT(_TREE) 술어를 만들 때 필요한 식별자만 노출.
 * 인증 정보가 없거나 사용자에 부서가 없으면 해당 필드는 null 또는 빈 집합.
 */
public record DataScopeContext(
        Long employeeId,
        Long departmentId,
        Set<Long> departmentSubtreeIds
) {
    public static DataScopeContext anonymous() {
        return new DataScopeContext(null, null, Set.of());
    }
}
