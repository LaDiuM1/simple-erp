package io.github.ladium1.erp.menu.api;

import io.github.ladium1.erp.menu.api.dto.MenuInfo;
import io.github.ladium1.erp.menu.internal.entity.Menu;

import java.util.List;

public interface MenuApi {
    /**
     * 모든 메뉴의 MenuInfo[id, name] 반환
     */
    List<MenuInfo> getAllMenus();

    MenuInfo getById(Long id);

    List<MenuInfo> getByIds(List<Long> ids);
}