package io.github.ladium1.erp.board.internal.repository;

import io.github.ladium1.erp.board.internal.dto.PostSearchCondition;
import io.github.ladium1.erp.board.internal.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PostRepositoryCustom {

    Page<Post> search(PostSearchCondition condition, Pageable pageable);
}
