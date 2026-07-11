package io.github.ladium1.erp.approval.internal.exception;

import io.github.ladium1.erp.global.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ApprovalErrorCode implements ErrorCode {

    DOCUMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 결재 문서입니다."),
    NOT_YOUR_TURN(HttpStatus.FORBIDDEN, "현재 결재 차례가 아닙니다."),
    ALREADY_COMPLETED(HttpStatus.CONFLICT, "이미 완료된 결재 문서입니다."),
    INVALID_APPROVAL_LINE(HttpStatus.BAD_REQUEST, "결재선이 올바르지 않습니다."),
    CANCEL_NOT_ALLOWED(HttpStatus.CONFLICT, "상신 취소할 수 없는 결재 문서입니다.");

    private final HttpStatus status;
    private final String message;

}
