package io.github.ladium1.erp.employee.internal.exception;

import io.github.ladium1.erp.global.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum EmployeeErrorCode implements ErrorCode {

    EMPLOYEE_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 직원입니다."),
    INVALID_PASSWORD(HttpStatus.UNAUTHORIZED, "비밀번호가 일치하지 않습니다."),
    DUPLICATE_LOGIN_ID(HttpStatus.CONFLICT, "이미 사용 중인 로그인 ID 입니다.");

    private final HttpStatus status;
    private final String message;

}
