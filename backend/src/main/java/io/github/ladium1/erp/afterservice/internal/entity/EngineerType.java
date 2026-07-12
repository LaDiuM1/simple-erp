package io.github.ladium1.erp.afterservice.internal.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 엔지니어 구분 — 자체 기술부 / 국내 외주 / 중국 제조사 (통역 동반 호출).
 */
@Getter
@RequiredArgsConstructor
public enum EngineerType {

    INTERNAL("내부"),
    OUTSOURCED("외주"),
    MANUFACTURER("제조사");

    private final String description;
}
