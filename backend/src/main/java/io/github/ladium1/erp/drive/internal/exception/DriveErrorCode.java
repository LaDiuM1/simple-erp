package io.github.ladium1.erp.drive.internal.exception;

import io.github.ladium1.erp.global.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum DriveErrorCode implements ErrorCode {

    FOLDER_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 폴더입니다."),
    FILE_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 파일입니다."),
    FOLDER_NOT_EMPTY(HttpStatus.CONFLICT, "하위 폴더나 파일이 있는 폴더는 삭제할 수 없습니다."),
    DUPLICATE_FOLDER_NAME(HttpStatus.CONFLICT, "같은 위치에 동일한 이름의 폴더가 이미 있습니다."),
    FILE_UPLOAD_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "파일 업로드 처리 중 오류가 발생했습니다.");

    private final HttpStatus status;
    private final String message;

}
