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
    POSITIONS("직책 관리"),
    CUSTOMERS("고객사 관리"),
    SUPPLIERS("공급사 관리"),
    PRODUCTS("제품 모델 관리"),
    SALES_CONTACTS("영업 명부 관리"),
    SALES_CUSTOMERS("고객사 영업 관리"),
    CONTRACTS("계약 관리"),
    EQUIPMENTS("설비 대장"),
    AFTER_SERVICES("AS 관리"),
    ROLES("권한 관리"),
    CODE_RULES("코드 채번 규칙"),
    APPROVALS("전자결재"),
    EXPENSES("경비 처리"),
    ATTENDANCE("근태 관리"),
    BOARDS("게시판"),
    DRIVE("드라이브");

    private final String label;
}
