package io.github.ladium1.erp.afterservice.internal.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * AS 유형 — 서비스 리포트 실무의 작업 분류 (수리 외에 설치 지원 / 교육 / 통역 / 조건셋팅 포함).
 */
@Getter
@RequiredArgsConstructor
public enum ServiceType {

    REPAIR("수리"),
    INSTALL_SUPPORT("설치지원"),
    TRAINING("교육"),
    INTERPRET("통역"),
    TUNING("조건셋팅");

    private final String description;
}
