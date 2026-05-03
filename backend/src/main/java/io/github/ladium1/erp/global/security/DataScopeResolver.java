package io.github.ladium1.erp.global.security;

import io.github.ladium1.erp.employee.api.EmployeeApi;
import io.github.ladium1.erp.global.menu.Menu;
import io.github.ladium1.erp.role.api.RoleApi;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * 현재 사용자 + 메뉴 -> 적용할 {@link DataScope} 결정.
 * <p>
 * 가시성을 적용하는 도메인 서비스가 호출. 인증이 없거나 메뉴 권한 행이 없으면 ALL 반환 —
 * 메뉴 진입은 {@link MenuPermissionEvaluator} 가 막으므로 여기 도달은 정상 권한자.
 */
@Component
@RequiredArgsConstructor
public class DataScopeResolver {

    private final EmployeeApi employeeApi;
    private final RoleApi roleApi;

    public DataScope resolve(Menu menu) {
        return resolve(menu, SecurityContextHolder.getContext().getAuthentication());
    }

    public DataScope resolve(Menu menu, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return DataScope.ALL;
        }
        Long roleId = employeeApi.getRoleIdByLoginId(authentication.getName());
        return roleApi.getMenuPermissionsByRoleId(roleId).stream()
                .filter(p -> p.menuCode() == menu)
                .findFirst()
                .map(p -> p.dataScope() == null ? DataScope.ALL : p.dataScope())
                .orElse(DataScope.ALL);
    }
}
