package io.github.ladium1.erp.product.internal.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 제품 카테고리 — 카탈로그 / 웹사이트의 제품 분류 체계와 동일.
 */
@Getter
@RequiredArgsConstructor
public enum ProductCategory {

    FLAT("평판 레이저"),
    H_BEAM("형강 레이저"),
    PIPE("파이프 레이저"),
    PRESS_BRAKE("절곡기"),
    COMBO("복합기"),
    DEBURRING("디버링기"),
    EDGE_MACHINE("엣지머신"),
    WELDER("용접기"),
    OSCILLATOR("발진기"),
    ETC("기타");

    private final String label;
}
