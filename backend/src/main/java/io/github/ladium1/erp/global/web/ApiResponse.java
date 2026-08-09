package io.github.ladium1.erp.global.web;

import com.fasterxml.jackson.annotation.JsonInclude;

public record ApiResponse<T>(
        int status,
        String message,
        @JsonInclude(JsonInclude.Include.NON_NULL)
        String code,
        @JsonInclude(JsonInclude.Include.NON_NULL)
        T data
) {
    /** 기존 Java 호출부와의 소스 호환용 생성자. */
    public ApiResponse(int status, String message, T data) {
        this(status, message, null, data);
    }

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(200, "요청이 성공적으로 처리되었습니다.", null, data);
    }

    public static ApiResponse<Void> noContent() {
        return new ApiResponse<>(204, "요청이 성공적으로 처리되었으며 반환 데이터는 없습니다.", null, null);
    }

    public static ApiResponse<Void> error(int status, String message) {
        return error(status, message, null);
    }

    public static ApiResponse<Void> error(int status, String message, String code) {
        return new ApiResponse<>(status, message, code, null);
    }
}
