package io.github.ladium1.erp.approval.internal.dto;

/**
 * 결재함 구분 — 목록 검색의 필수 조건. 현재 사용자 employeeId 기준으로 문서를 거른다.
 */
public enum ApprovalBox {

    /** 내가 기안한 문서 */
    DRAFTED,

    /** 내 결재 차례인 문서 */
    PENDING,

    /** 내가 결재 처리 (승인 / 반려) 한 문서 */
    PROCESSED,

    /** 기안자이거나 결재선에 포함된 전체 문서 */
    INVOLVED
}
