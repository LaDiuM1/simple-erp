package io.github.ladium1.erp.board.internal.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import io.github.ladium1.erp.board.internal.dto.PostSearchCondition;
import io.github.ladium1.erp.board.internal.entity.Post;
import io.github.ladium1.erp.board.internal.entity.QPost;
import io.github.ladium1.erp.global.jpa.QuerydslSortUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.util.StringUtils;

import java.util.List;

@RequiredArgsConstructor
public class PostRepositoryImpl implements PostRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<Post> search(PostSearchCondition condition, Pageable pageable) {
        QPost p = QPost.post;
        BooleanBuilder where = buildPredicate(condition, p);

        List<Post> content = queryFactory
                .selectFrom(p)
                .where(where)
                .orderBy(QuerydslSortUtils.toOrderSpecifiers(pageable.getSort(), java.util.Map.of(
                        "id", p.id, "category", p.category, "title", p.title, "createdAt", p.createdAt), p.id.desc()))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = queryFactory
                .select(p.count())
                .from(p)
                .where(where)
                .fetchOne();

        return new PageImpl<>(content, pageable, total != null ? total : 0L);
    }

    private BooleanBuilder buildPredicate(PostSearchCondition condition, QPost p) {
        BooleanBuilder where = new BooleanBuilder();
        if (condition.category() != null) {
            where.and(p.category.eq(condition.category()));
        }
        if (StringUtils.hasText(condition.keyword())) {
            where.and(p.title.like("%" + condition.keyword().trim() + "%"));
        }
        if (condition.authorId() != null) {
            where.and(p.authorId.eq(condition.authorId()));
        }
        return where;
    }
}
