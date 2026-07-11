package io.github.ladium1.erp.board.internal.exception;

import io.github.ladium1.erp.global.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum BoardErrorCode implements ErrorCode {

    POST_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 게시글입니다."),
    COMMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 댓글입니다."),
    NOT_AUTHOR(HttpStatus.FORBIDDEN, "작성자만 수정/삭제할 수 있습니다."),
    NOTICE_REQUIRES_WRITE_PERMISSION(HttpStatus.FORBIDDEN, "공지사항은 게시판 쓰기 권한자만 작성할 수 있습니다.");

    private final HttpStatus status;
    private final String message;

}
