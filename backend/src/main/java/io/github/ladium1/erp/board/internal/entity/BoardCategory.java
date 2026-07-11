package io.github.ladium1.erp.board.internal.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 게시판 카테고리 — NOTICE 는 BOARDS 메뉴 write 권한자만 작성/수정 가능.
 */
@Getter
@RequiredArgsConstructor
public enum BoardCategory {

    MEETING("회의록"),
    NOTICE("공지사항"),
    FREE("자유");

    private final String label;
}
