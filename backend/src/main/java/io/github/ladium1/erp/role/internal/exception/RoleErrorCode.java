package io.github.ladium1.erp.role.internal.exception;

import io.github.ladium1.erp.global.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum RoleErrorCode implements ErrorCode {

    ROLE_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 권한입니다."),
    DUPLICATE_ROLE_CODE(HttpStatus.CONFLICT, "이미 존재하는 권한 코드입니다.");

    private final HttpStatus status;
    private final String message;

}
