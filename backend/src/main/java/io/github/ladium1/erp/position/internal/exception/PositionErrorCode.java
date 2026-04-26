package io.github.ladium1.erp.position.internal.exception;

import io.github.ladium1.erp.global.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum PositionErrorCode implements ErrorCode {

    POSITION_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 직책입니다."),
    DUPLICATE_CODE(HttpStatus.CONFLICT, "이미 사용 중인 직책 코드입니다."),
    CODE_REQUIRED(HttpStatus.BAD_REQUEST, "직책 코드를 입력해주세요.");

    private final HttpStatus status;
    private final String message;

}
