package io.github.ladium1.erp.approval.api;

/**
 * 결재 결과 통지 SPI — 결재를 태운 도메인 모듈이 빈으로 구현.
 * <p>
 * 문서가 최종 승인 / 반려 (상신 취소 포함) 되면 approval 모듈이 docType 매칭
 * 핸들러의 콜백을 같은 트랜잭션 안에서 호출한다. GENERAL 등 핸들러 미등록 유형은 no-op.
 */
public interface ApprovalResultHandler {

    /** 이 핸들러가 담당하는 문서 유형 */
    ApprovalDocType docType();

    /** 최종 승인 시 — refId = 연동 도메인 레코드 ID */
    void onApproved(Long refId);

    /** 반려 / 상신 취소 시 — refId = 연동 도메인 레코드 ID */
    void onRejected(Long refId);
}
