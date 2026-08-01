package io.github.ladium1.erp.global.jpa;

import com.querydsl.core.types.Expression;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import io.github.ladium1.erp.global.exception.BusinessException;
import org.springframework.data.domain.Sort;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Spring Data {@link Sort} → QueryDSL {@link OrderSpecifier} 변환 유틸.
 */
public final class QuerydslSortUtils {

    private QuerydslSortUtils() {
    }

    /**
     * 정렬 조건이 비어있으면 {@code defaultOrder} 한 개를 사용한다.
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    public static OrderSpecifier<?>[] toOrderSpecifiers(Sort sort,
                                                        Map<String, ? extends Expression<? extends Comparable<?>>> allowedProperties,
                                                        OrderSpecifier<?> defaultOrder) {
        Expression<? extends Comparable<?>> idExpression = allowedProperties.get("id");
        if (sort == null || sort.isUnsorted()) {
            if (idExpression == null || defaultOrder.getTarget().equals(idExpression)) {
                return new OrderSpecifier[]{defaultOrder};
            }
            return new OrderSpecifier[]{defaultOrder, new OrderSpecifier(Order.ASC, idExpression)};
        }

        List<OrderSpecifier> orders = new ArrayList<>();
        for (Sort.Order order : sort) {
            Expression<? extends Comparable<?>> expression = allowedProperties.get(order.getProperty());
            if (expression == null) {
                throw new BusinessException(QueryErrorCode.INVALID_SORT_PROPERTY);
            }
            Order direction = order.isAscending() ? Order.ASC : Order.DESC;
            orders.add(new OrderSpecifier(direction, expression));
        }
        if (idExpression != null && sort.getOrderFor("id") == null) {
            orders.add(new OrderSpecifier(Order.ASC, idExpression));
        }
        return orders.toArray(new OrderSpecifier[0]);
    }
}
