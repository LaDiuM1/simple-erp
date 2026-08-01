package io.github.ladium1.erp.equipment.internal.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import io.github.ladium1.erp.equipment.internal.dto.EquipmentSearchCondition;
import io.github.ladium1.erp.equipment.internal.entity.Equipment;
import io.github.ladium1.erp.equipment.internal.entity.QEquipment;
import io.github.ladium1.erp.global.jpa.QuerydslSortUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.util.List;

@RequiredArgsConstructor
public class EquipmentRepositoryImpl implements EquipmentRepositoryCustom {

    /** 만료 임박 판정 구간 — 오늘부터 90일. */
    private static final int EXPIRING_WINDOW_DAYS = 90;

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<Equipment> search(EquipmentSearchCondition condition, Pageable pageable) {
        QEquipment e = QEquipment.equipment;
        BooleanBuilder where = buildPredicate(condition, e);

        List<Equipment> content = queryFactory
                .selectFrom(e)
                .where(where)
                .orderBy(QuerydslSortUtils.toOrderSpecifiers(pageable.getSort(), sortProperties(e), e.id.desc()))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = queryFactory
                .select(e.count())
                .from(e)
                .where(where)
                .fetchOne();

        return new PageImpl<>(content, pageable, total != null ? total : 0L);
    }

    @Override
    public List<Equipment> searchAll(EquipmentSearchCondition condition, Sort sort) {
        QEquipment e = QEquipment.equipment;
        return queryFactory
                .selectFrom(e)
                .where(buildPredicate(condition, e))
                .orderBy(QuerydslSortUtils.toOrderSpecifiers(sort, sortProperties(e), e.id.desc()))
                .fetch();
    }

    @Override
    public List<Equipment> findExpiringWarranties(int days, int limit) {
        QEquipment e = QEquipment.equipment;
        LocalDate today = LocalDate.now();
        LocalDate until = today.plusDays(days);
        return queryFactory
                .selectFrom(e)
                .where(
                        e.generalWarrantyEndDate.between(today, until)
                                .or(e.oscillatorWarrantyEndDate.between(today, until))
                )
                .orderBy(e.generalWarrantyEndDate.asc().nullsLast(), e.oscillatorWarrantyEndDate.asc())
                .limit(limit)
                .fetch();
    }

    private BooleanBuilder buildPredicate(EquipmentSearchCondition condition, QEquipment e) {
        BooleanBuilder where = new BooleanBuilder();
        if (condition == null) {
            return where;
        }
        if (condition.customerId() != null) {
            where.and(e.customerId.eq(condition.customerId()));
        }
        if (condition.supplierId() != null) {
            where.and(e.supplierId.eq(condition.supplierId()));
        }
        if (StringUtils.hasText(condition.serialKeyword())) {
            where.and(e.serialNo.like("%" + condition.serialKeyword().trim() + "%"));
        }
        if (StringUtils.hasText(condition.addressKeyword())) {
            where.and(e.installAddress.like("%" + condition.addressKeyword().trim() + "%"));
        }
        if (condition.warranty() != null) {
            LocalDate today = LocalDate.now();
            switch (condition.warranty()) {
                case ACTIVE -> where.and(e.generalWarrantyEndDate.goe(today));
                case EXPIRED -> where.and(e.generalWarrantyEndDate.lt(today));
                case EXPIRING -> {
                    LocalDate limit = today.plusDays(EXPIRING_WINDOW_DAYS);
                    where.and(
                            e.generalWarrantyEndDate.between(today, limit)
                                    .or(e.oscillatorWarrantyEndDate.between(today, limit))
                    );
                }
            }
        }
        return where;
    }

    private static java.util.Map<String, ? extends com.querydsl.core.types.Expression<? extends Comparable<?>>> sortProperties(QEquipment e) {
        return java.util.Map.of(
                "id", e.id,
                "serialNo", e.serialNo,
                "installedDate", e.installedDate,
                "oscillatorWarrantyEndDate", e.oscillatorWarrantyEndDate,
                "generalWarrantyEndDate", e.generalWarrantyEndDate
        );
    }
}
