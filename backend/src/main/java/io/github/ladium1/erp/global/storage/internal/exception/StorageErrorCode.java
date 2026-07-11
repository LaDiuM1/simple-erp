package io.github.ladium1.erp.global.storage.internal.exception;

import io.github.ladium1.erp.global.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum StorageErrorCode implements ErrorCode {

    FILE_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 파일입니다."),
    STORAGE_IO_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "파일 저장소 처리 중 오류가 발생했습니다."),
    EMPTY_FILE(HttpStatus.BAD_REQUEST, "빈 파일은 업로드할 수 없습니다.");

    private final HttpStatus status;
    private final String message;

}
