package io.github.ladium1.erp.board.internal.entity;

import io.github.ladium1.erp.global.jpa.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "post_comments")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PostComment extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @Column(name = "author_id", nullable = false,
            comment = "댓글 작성자 직원 식별자")
    private Long authorId;

    @Column(nullable = false, length = 1000,
            comment = "댓글 내용")
    private String content;

    @Builder
    PostComment(Post post, Long authorId, String content) {
        this.post = post;
        this.authorId = authorId;
        this.content = content;
    }

    public boolean isAuthor(Long employeeId) {
        return authorId.equals(employeeId);
    }
}
