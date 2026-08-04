package io.github.ladium1.erp.global.security;

import io.github.ladium1.erp.global.menu.Menu;

import java.util.EnumSet;
import java.util.Set;

/**
 * 행 단위 데이터 범위를 실제로 적용하는 메뉴의 서버 정책.
 */
public final class DataScopePolicy {

    private static final Set<Menu> SUPPORTED_MENUS = EnumSet.of(
            Menu.SALES_CUSTOMERS,
            Menu.CONTRACTS
    );

    private DataScopePolicy() {
    }

    public static boolean supports(Menu menu) {
        return SUPPORTED_MENUS.contains(menu);
    }

    public static DataScope normalize(Menu menu, DataScope scope) {
        if (!supports(menu) || scope == null) {
            return DataScope.ALL;
        }
        return scope;
    }
}
