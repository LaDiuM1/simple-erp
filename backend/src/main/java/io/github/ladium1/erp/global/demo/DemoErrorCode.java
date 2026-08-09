package io.github.ladium1.erp.global.demo;

import io.github.ladium1.erp.global.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum DemoErrorCode implements ErrorCode {
    DEMO_RESET_IN_PROGRESS(
            HttpStatus.SERVICE_UNAVAILABLE,
            "데모 데이터를 초기화하거나 검증하고 있습니다. 잠시 후 다시 시도해 주세요."
    ),
    DEMO_UPLOAD_DISABLED(
            HttpStatus.FORBIDDEN,
            "데모에서는 파일 업로드와 기존 파일 ID 첨부가 비활성화되어 있습니다."
    ),
    DEMO_PROTECTED_RESOURCE(
            HttpStatus.FORBIDDEN,
            "데모 복구에 필요한 보호 리소스는 변경할 수 없습니다."
    ),
    DEMO_RATE_LIMIT_EXCEEDED(
            HttpStatus.TOO_MANY_REQUESTS,
            "데모 요청 한도를 초과했습니다. 잠시 후 다시 시도해 주세요."
    );

    private final HttpStatus status;
    private final String message;

    @Override
    public String getCode() {
        return name();
    }
}
