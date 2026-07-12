package io.github.ladium1.erp.contract.internal.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 정부 지원사업 진행 상태 — 선정 여부가 계약 성사 / 대금 스케줄에 직결되는 실무 반영.
 */
@Getter
@RequiredArgsConstructor
public enum SupportProgramStatus {

    NONE("해당없음"),
    APPLIED("신청"),
    SELECTED("선정"),
    REJECTED("미선정");

    private final String description;
}
