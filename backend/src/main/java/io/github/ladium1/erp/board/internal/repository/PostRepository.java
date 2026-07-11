package io.github.ladium1.erp.board.internal.repository;

import io.github.ladium1.erp.board.internal.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostRepository extends JpaRepository<Post, Long>, PostRepositoryCustom {
}
