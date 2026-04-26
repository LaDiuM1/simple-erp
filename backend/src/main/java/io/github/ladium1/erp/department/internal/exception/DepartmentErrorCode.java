package io.github.ladium1.erp.department.internal.exception;

import io.github.ladium1.erp.global.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum DepartmentErrorCode implements ErrorCode {

    DEPARTMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 부서입니다."),
    DUPLICATE_CODE(HttpStatus.CONFLICT, "이미 사용 중인 부서 코드입니다."),
    CODE_REQUIRED(HttpStatus.BAD_REQUEST, "부서 코드를 입력해주세요."),
    CANNOT_SET_SELF_AS_PARENT(HttpStatus.BAD_REQUEST, "부서를 자신의 상위 부서로 설정할 수 없습니다."),
    CANNOT_DELETE_HAS_CHILDREN(HttpStatus.BAD_REQUEST, "하위 부서가 있는 부서는 삭제할 수 없습니다.");

    private final HttpStatus status;
    private final String message;

}
