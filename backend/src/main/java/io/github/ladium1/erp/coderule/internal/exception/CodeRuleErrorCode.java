package io.github.ladium1.erp.coderule.internal.exception;

import io.github.ladium1.erp.global.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum CodeRuleErrorCode implements ErrorCode {

    RULE_NOT_FOUND(HttpStatus.NOT_FOUND, "등록된 채번 규칙이 없습니다."),
    INVALID_PATTERN(HttpStatus.BAD_REQUEST, "코드 패턴이 올바르지 않습니다."),
    CODE_FORMAT_MISMATCH(HttpStatus.BAD_REQUEST, "입력한 코드가 채번 규칙과 일치하지 않습니다."),
    MISSING_PARENT_CONTEXT(HttpStatus.BAD_REQUEST, "{PARENT} 토큰을 사용하려면 부모 코드가 필요합니다.");

    private final HttpStatus status;
    private final String message;
}
