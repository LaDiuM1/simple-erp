package io.github.ladium1.erp.approval.internal.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import io.github.ladium1.erp.approval.internal.dto.ApprovalBox;
import io.github.ladium1.erp.approval.internal.dto.ApprovalSearchCondition;
import io.github.ladium1.erp.approval.internal.entity.ApprovalDocument;
import io.github.ladium1.erp.approval.internal.entity.ApprovalStatus;
import io.github.ladium1.erp.approval.internal.entity.QApprovalDocument;
import io.github.ladium1.erp.approval.internal.entity.QApprovalStep;
import io.github.ladium1.erp.approval.internal.entity.StepStatus;
import io.github.ladium1.erp.global.jpa.QuerydslSortUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.util.StringUtils;

import java.util.List;

@RequiredArgsConstructor
public class ApprovalDocumentRepositoryImpl implements ApprovalDocumentRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<ApprovalDocument> search(Long employeeId, ApprovalSearchCondition condition, Pageable pageable) {
        QApprovalDocument d = QApprovalDocument.approvalDocument;
        BooleanBuilder where = buildPredicate(employeeId, condition, d);

        List<ApprovalDocument> content = queryFactory
                .selectFrom(d)
                .where(where)
                .orderBy(QuerydslSortUtils.toOrderSpecifiers(pageable.getSort(), d, d.id.desc()))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = queryFactory
                .select(d.count())
                .from(d)
                .where(where)
                .fetchOne();

        return new PageImpl<>(content, pageable, total != null ? total : 0L);
    }

    private BooleanBuilder buildPredicate(Long employeeId, ApprovalSearchCondition condition, QApprovalDocument d) {
        BooleanBuilder where = new BooleanBuilder();
        where.and(boxPredicate(condition.box(), employeeId, d));
        if (condition.status() != null) {
            where.and(d.status.eq(condition.status()));
        }
        if (condition.docType() != null) {
            where.and(d.docType.eq(condition.docType()));
        }
        if (StringUtils.hasText(condition.keyword())) {
            where.and(d.title.like("%" + condition.keyword().trim() + "%"));
        }
        return where;
    }

    private BooleanExpression boxPredicate(ApprovalBox box, Long employeeId, QApprovalDocument d) {
        QApprovalStep s = QApprovalStep.approvalStep;
        return switch (box) {
            case DRAFTED -> d.drafterId.eq(employeeId);
            case PENDING -> d.status.eq(ApprovalStatus.IN_PROGRESS)
                    .and(JPAExpressions.selectOne()
                            .from(s)
                            .where(s.document.eq(d),
                                    s.approverId.eq(employeeId),
                                    s.stepOrder.eq(d.currentStepOrder),
                                    s.status.eq(StepStatus.PENDING))
                            .exists());
            case PROCESSED -> JPAExpressions.selectOne()
                    .from(s)
                    .where(s.document.eq(d),
                            s.approverId.eq(employeeId),
                            s.status.ne(StepStatus.PENDING))
                    .exists();
            case INVOLVED -> d.drafterId.eq(employeeId)
                    .or(JPAExpressions.selectOne()
                            .from(s)
                            .where(s.document.eq(d), s.approverId.eq(employeeId))
                            .exists());
        };
    }
}
