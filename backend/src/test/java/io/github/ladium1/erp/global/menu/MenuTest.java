package io.github.ladium1.erp.global.menu;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MenuTest {

    @Test
    void permissionRequestLimitMatchesMenuCatalog() {
        assertThat(Menu.values()).hasSize(Menu.MAX_PERMISSION_COUNT);
    }
}
