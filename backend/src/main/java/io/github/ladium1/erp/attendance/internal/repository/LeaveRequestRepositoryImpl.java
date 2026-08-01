package io.github.ladium1.erp.attendance.internal.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import io.github.ladium1.erp.attendance.internal.dto.LeaveSearchCondition;
import io.github.ladium1.erp.attendance.internal.entity.LeaveRequest;
import io.github.ladium1.erp.attendance.internal.entity.QLeaveRequest;
import io.github.ladium1.erp.global.jpa.QuerydslSortUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;

@RequiredArgsConstructor
public class LeaveRequestRepositoryImpl implements LeaveRequestRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<LeaveRequest> search(LeaveSearchCondition condition, Pageable pageable) {
        QLeaveRequest l = QLeaveRequest.leaveRequest;
        BooleanBuilder where = buildPredicate(condition, l);

        List<LeaveRequest> content = queryFactory
                .selectFrom(l)
                .where(where)
                .orderBy(QuerydslSortUtils.toOrderSpecifiers(pageable.getSort(), java.util.Map.of(
                        "id", l.id, "startDate", l.startDate, "createdAt", l.createdAt), l.id.desc()))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = queryFactory
                .select(l.count())
                .from(l)
                .where(where)
                .fetchOne();

        return new PageImpl<>(content, pageable, total != null ? total : 0L);
    }

    private BooleanBuilder buildPredicate(LeaveSearchCondition condition, QLeaveRequest l) {
        BooleanBuilder where = new BooleanBuilder();
        if (condition.status() != null) {
            where.and(l.status.eq(condition.status()));
        }
        if (condition.employeeId() != null) {
            where.and(l.employeeId.eq(condition.employeeId()));
        }
        // 기간 조건은 겹침 판정 — 신청 기간이 검색 구간에 하루라도 걸치면 포함
        if (condition.startDate() != null) {
            where.and(l.endDate.goe(condition.startDate()));
        }
        if (condition.endDate() != null) {
            where.and(l.startDate.loe(condition.endDate()));
        }
        return where;
    }
}
