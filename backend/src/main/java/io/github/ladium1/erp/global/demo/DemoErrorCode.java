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
    ),
    DEMO_UPLOAD_QUOTA_EXCEEDED(
            HttpStatus.TOO_MANY_REQUESTS,
            "데모 업로드 한도를 초과했습니다. 다음 초기화 후 다시 시도해 주세요."
    ),
    DEMO_EXCEL_ROW_QUOTA_EXCEEDED(
            HttpStatus.TOO_MANY_REQUESTS,
            "데모 엑셀 등록 한도를 초과했습니다. 다음 초기화 후 다시 시도해 주세요."
    ),
    DEMO_EXCEL_EXPORT_TOO_LARGE(
            HttpStatus.TOO_MANY_REQUESTS,
            "데모 엑셀 다운로드 행 한도를 초과했습니다. 검색 조건을 줄이거나 다음 초기화 후 다시 시도해 주세요."
    ),
    DEMO_STORAGE_UNAVAILABLE(
            HttpStatus.INSUFFICIENT_STORAGE,
            "데모 파일 저장소의 안전 여유 공간을 확보할 수 없습니다."
    ),
    DEMO_UNSUPPORTED_MULTIPART(
            HttpStatus.UNSUPPORTED_MEDIA_TYPE,
            "데모에서 지원하지 않는 multipart 요청입니다."
    );

    private final HttpStatus status;
    private final String message;

    @Override
    public String getCode() {
        return name();
    }
}
