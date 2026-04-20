package io.github.ladium1.erp.global.api;

import com.fasterxml.jackson.annotation.JsonInclude;

public record ApiResponse<T>(
        int status,
        String message,
        @JsonInclude(JsonInclude.Include.NON_NULL)
        T data
) {
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(200, "요청이 성공적으로 처리되었습니다.", data);
    }

    public static ApiResponse<Void> noContent() {
        return new ApiResponse<>(204, "요청이 성공적으로 처리되었으며 반환 데이터는 없습니다.", null);
    }

    public static ApiResponse<Void> error(int status, String message) {
        return new ApiResponse<>(status, message, null);
    }
}