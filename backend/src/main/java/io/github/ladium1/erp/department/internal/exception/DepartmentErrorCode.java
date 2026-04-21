package io.github.ladium1.erp.department.internal.exception;

import io.github.ladium1.erp.global.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum DepartmentErrorCode implements ErrorCode {

    DEPARTMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 부서입니다.");

    private final HttpStatus status;
    private final String message;

}
