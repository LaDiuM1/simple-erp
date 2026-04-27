package io.github.ladium1.erp.salescontact.internal.exception;

import io.github.ladium1.erp.global.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum AcquisitionSourceErrorCode implements ErrorCode {

    SOURCE_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 컨택 경로입니다."),
    DUPLICATE_NAME(HttpStatus.CONFLICT, "이미 사용 중인 컨택 경로 이름입니다.");

    private final HttpStatus status;
    private final String message;
}
