package io.github.ladium1.erp.global.security;

import io.github.ladium1.erp.global.menu.Menu;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DataScopePolicyTest {

    @Test
    void supportedMenusKeepRequestedScope() {
        assertThat(DataScopePolicy.normalize(Menu.SALES_CUSTOMERS, DataScope.DEPARTMENT))
                .isEqualTo(DataScope.DEPARTMENT);
        assertThat(DataScopePolicy.normalize(Menu.CONTRACTS, DataScope.DEPARTMENT_TREE))
                .isEqualTo(DataScope.DEPARTMENT_TREE);
    }

    @Test
    void unsupportedMenusAlwaysUseAllScope() {
        assertThat(DataScopePolicy.normalize(Menu.CUSTOMERS, DataScope.SELF)).isEqualTo(DataScope.ALL);
        assertThat(DataScopePolicy.normalize(Menu.EMPLOYEES, DataScope.SELF)).isEqualTo(DataScope.ALL);
        assertThat(DataScopePolicy.normalize(Menu.EQUIPMENTS, DataScope.DEPARTMENT)).isEqualTo(DataScope.ALL);
        assertThat(DataScopePolicy.normalize(Menu.AFTER_SERVICES, DataScope.DEPARTMENT_TREE))
                .isEqualTo(DataScope.ALL);
    }
}
