package io.github.ladium1.erp.board.internal.entity;

import io.github.ladium1.erp.global.jpa.BaseEntity;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Table(name = "posts",
        indexes = {
                @Index(name = "idx_posts_category", columnList = "category"),
                @Index(name = "idx_posts_author_id", columnList = "author_id")
        })
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Post extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false,
            comment = "게시판 카테고리")
    private BoardCategory category;

    @Column(nullable = false, length = 200,
            comment = "제목")
    private String title;

    @Lob
    @Column(nullable = false,
            comment = "본문")
    private String content;

    @Column(name = "author_id", nullable = false,
            comment = "작성자 직원 식별자")
    private Long authorId;

    @ElementCollection
    @CollectionTable(name = "post_attachment_files", joinColumns = @JoinColumn(name = "post_id"))
    @OrderColumn(name = "attachment_order")
    @Column(name = "file_id", nullable = false,
            comment = "첨부 파일 식별자 — global/storage 참조")
    private List<Long> attachmentFileIds = new ArrayList<>();

    @Builder
    Post(BoardCategory category, String title, String content, Long authorId, List<Long> attachmentFileIds) {
        this.category = category;
        this.title = title;
        this.content = content;
        this.authorId = authorId;
        this.attachmentFileIds = attachmentFileIds == null ? new ArrayList<>() : new ArrayList<>(attachmentFileIds);
    }

    public void update(BoardCategory category, String title, String content, List<Long> attachmentFileIds) {
        this.category = category;
        this.title = title;
        this.content = content;
        this.attachmentFileIds.clear();
        if (attachmentFileIds != null) {
            this.attachmentFileIds.addAll(attachmentFileIds);
        }
    }

    public boolean isAuthor(Long employeeId) {
        return authorId.equals(employeeId);
    }
}
