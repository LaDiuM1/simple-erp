package io.github.ladium1.erp.global.security;

import io.github.ladium1.erp.employee.api.EmployeeApi;
import io.github.ladium1.erp.global.menu.Menu;
import io.github.ladium1.erp.role.api.RoleApi;
import io.github.ladium1.erp.role.api.dto.MenuPermission;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.List;

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
        return scopeOf(loadPermissions(authentication), menu);
    }

    /**
     * 한 행을 여러 메뉴로 진입할 수 있는 컨트롤러 (예: CUSTOMERS + SALES_CUSTOMERS) 에서 사용.
     * 사용자가 가진 메뉴들 중 가장 permissive 한 스코프를 반환. 메뉴에 read 권한이 없으면 기여 안 함.
     */
    public DataScope resolveMostPermissive(Menu... menus) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return DataScope.ALL;
        }
        List<MenuPermission> permissions = loadPermissions(auth);
        DataScope effective = null;
        for (Menu m : menus) {
            MenuPermission p = permissions.stream().filter(x -> x.menuCode() == m && x.canRead()).findFirst().orElse(null);
            if (p == null) continue;
            DataScope s = p.dataScope() == null ? DataScope.ALL : p.dataScope();
            if (s == DataScope.ALL) return DataScope.ALL;
            if (effective == null || s.isMorePermissiveThan(effective)) effective = s;
        }
        // 어떤 메뉴도 read 권한 없음 — 컨트롤러 단에서 막혔어야 정상. 안전 우선으로 가장 좁게.
        return effective == null ? DataScope.SELF : effective;
    }

    private List<MenuPermission> loadPermissions(Authentication authentication) {
        Long roleId = employeeApi.getRoleIdByLoginId(authentication.getName());
        return roleApi.getMenuPermissionsByRoleId(roleId);
    }

    private DataScope scopeOf(List<MenuPermission> permissions, Menu menu) {
        return permissions.stream()
                .filter(p -> p.menuCode() == menu)
                .findFirst()
                .map(p -> p.dataScope() == null ? DataScope.ALL : p.dataScope())
                .orElse(DataScope.ALL);
    }
}
