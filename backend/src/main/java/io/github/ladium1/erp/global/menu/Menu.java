package io.github.ladium1.erp.global.menu;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 시스템 메뉴 enum.
 */
@Getter
@RequiredArgsConstructor
public enum Menu {

    MDM_HRM("직원 관리");

    private final String label;
}
