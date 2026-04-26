package io.github.ladium1.erp.global.menu;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 시스템 메뉴 enum.
 */
@Getter
@RequiredArgsConstructor
public enum Menu {

    EMPLOYEES("직원 관리"),
    DEPARTMENTS("부서 관리"),
    CODE_RULES("코드 채번 규칙");

    private final String label;
}
