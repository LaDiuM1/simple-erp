package io.github.ladium1.erp.board.internal.repository;

import io.github.ladium1.erp.board.internal.entity.PostComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface PostCommentRepository extends JpaRepository<PostComment, Long> {

    List<PostComment> findByPostIdOrderByIdAsc(Long postId);

    void deleteByPostId(Long postId);

    /**
     * 여러 게시글의 댓글 수를 한 번에 집계 — 목록 페이지 보강용.
     */
    @Query("""
            select new io.github.ladium1.erp.board.internal.repository.PostCommentCount(
                c.post.id, count(c)
            )
            from PostComment c
            where c.post.id in :postIds
            group by c.post.id
            """)
    List<PostCommentCount> countByPostIds(@Param("postIds") Collection<Long> postIds);
}
