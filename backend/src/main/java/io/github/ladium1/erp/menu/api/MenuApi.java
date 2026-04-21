package io.github.ladium1.erp.menu.api;

import io.github.ladium1.erp.menu.api.dto.MenuInfo;

import java.util.List;

public interface MenuApi {
    /**
     * 모든 메뉴의 MenuInfo[id, name] 반환
     */
    List<MenuInfo> getAllMenus();

}