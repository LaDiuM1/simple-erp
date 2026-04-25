package io.github.ladium1.erp.global.security;

import io.github.ladium1.erp.member.api.MemberApi;
import io.github.ladium1.erp.role.api.RoleApi;
import io.github.ladium1.erp.role.api.dto.MenuPermission;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

/**
 * 메뉴 권한 검증 컴포넌트
 * <p>
 * {@code @PreAuthorize("@menuPermissionEvaluator.canRead(authentication, 'XXX')")} 형태로 호출.
 */
@Component("menuPermissionEvaluator")
@RequiredArgsConstructor
public class MenuPermissionEvaluator {

    private final MemberApi memberApi;
    private final RoleApi roleApi;

    public boolean canRead(Authentication authentication, String menuCode) {
        return hasPermission(authentication, menuCode, MenuPermission::canRead);
    }

    public boolean canWrite(Authentication authentication, String menuCode) {
        return hasPermission(authentication, menuCode, MenuPermission::canWrite);
    }

    private boolean hasPermission(Authentication authentication, String menuCode, java.util.function.Predicate<MenuPermission> check) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }
        Long roleId = memberApi.getRoleIdByLoginId(authentication.getName());
        return roleApi.getMenuPermissionsByRoleId(roleId).stream()
                .filter(p -> p.menuCode().name().equals(menuCode))
                .findFirst()
                .map(check::test)
                .orElse(false);
    }
}
