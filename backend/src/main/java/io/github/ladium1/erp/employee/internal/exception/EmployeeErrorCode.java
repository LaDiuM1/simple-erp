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
    DUPLICATE_LOGIN_ID(HttpStatus.CONFLICT, "이미 사용 중인 로그인 ID 입니다."),
    EMPLOYEE_EXISTS_IN_DEPARTMENT(HttpStatus.BAD_REQUEST, "직원이 속한 부서는 삭제할 수 없습니다."),
    EMPLOYEE_EXISTS_IN_POSITION(HttpStatus.BAD_REQUEST, "직원이 속한 직책은 삭제할 수 없습니다.");

    private final HttpStatus status;
    private final String message;

}
