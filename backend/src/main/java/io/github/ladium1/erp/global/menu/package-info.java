/**
 * 시스템 메뉴 enum 패키지.
 * <p>
 * 메뉴는 정적 정의로 코드를 단일 진실 소스로 삼는다. role_menus 테이블이
 * enum name 을 문자열로 참조하며, 권한 평가는 menuCode 문자열 비교로 이뤄진다.
 */
@org.springframework.modulith.NamedInterface("menu")
package io.github.ladium1.erp.global.menu;
