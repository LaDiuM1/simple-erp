package io.github.ladium1.erp.menus;

import java.util.List;

public interface MenuApi {
    /**
     * 모든 메뉴의 MenuInfo[id, name] 반환
     */
    List<MenuInfo> getAllMenus();

}