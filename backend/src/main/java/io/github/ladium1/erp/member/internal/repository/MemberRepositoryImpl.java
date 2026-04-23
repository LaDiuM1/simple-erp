package io.github.ladium1.erp.member.internal.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import io.github.ladium1.erp.global.jpa.QuerydslSortUtils;
import io.github.ladium1.erp.member.internal.dto.MemberSearchCondition;
import io.github.ladium1.erp.member.internal.entity.Member;
import io.github.ladium1.erp.member.internal.entity.QMember;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.util.StringUtils;

import java.util.List;

@RequiredArgsConstructor
public class MemberRepositoryImpl implements MemberRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<Member> search(MemberSearchCondition condition, Pageable pageable) {
        QMember m = QMember.member;
        BooleanBuilder where = buildPredicate(condition, m);

        List<Member> content = queryFactory
                .selectFrom(m)
                .where(where)
                .orderBy(QuerydslSortUtils.toOrderSpecifiers(pageable.getSort(), m, m.id.desc()))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = queryFactory
                .select(m.count())
                .from(m)
                .where(where)
                .fetchOne();

        return new PageImpl<>(content, pageable, total != null ? total : 0L);
    }

    @Override
    public List<Member> searchAll(MemberSearchCondition condition, Sort sort) {
        QMember m = QMember.member;
        return queryFactory
                .selectFrom(m)
                .where(buildPredicate(condition, m))
                .orderBy(QuerydslSortUtils.toOrderSpecifiers(sort, m, m.id.desc()))
                .fetch();
    }

    private BooleanBuilder buildPredicate(MemberSearchCondition condition, QMember m) {
        BooleanBuilder where = new BooleanBuilder();
        if (condition == null) {
            return where;
        }
        if (StringUtils.hasText(condition.keyword())) {
            String like = "%" + condition.keyword().trim() + "%";
            where.and(m.name.like(like).or(m.loginId.like(like)));
        }
        if (condition.departmentId() != null) {
            where.and(m.departmentId.eq(condition.departmentId()));
        }
        if (condition.positionId() != null) {
            where.and(m.positionId.eq(condition.positionId()));
        }
        if (condition.roleId() != null) {
            where.and(m.roleId.eq(condition.roleId()));
        }
        if (condition.status() != null) {
            where.and(m.status.eq(condition.status()));
        }
        return where;
    }
}
