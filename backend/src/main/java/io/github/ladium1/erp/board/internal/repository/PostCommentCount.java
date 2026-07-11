package io.github.ladium1.erp.board.internal.repository;

/**
 * 게시글별 댓글 수 집계용 record. JPQL constructor expression 으로 생성.
 */
public record PostCommentCount(Long postId, long count) {
}
