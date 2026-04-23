package io.github.ladium1.erp.global.jpa;

import com.querydsl.core.types.EntityPath;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.PathBuilder;
import org.springframework.data.domain.Sort;

import java.util.ArrayList;
import java.util.List;

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
                                                        EntityPath<?> entityPath,
                                                        OrderSpecifier<?> defaultOrder) {
        if (sort == null || sort.isUnsorted()) {
            return new OrderSpecifier[]{defaultOrder};
        }

        PathBuilder<?> path = new PathBuilder<>(entityPath.getType(), entityPath.getMetadata());
        List<OrderSpecifier> orders = new ArrayList<>();
        for (Sort.Order order : sort) {
            Order direction = order.isAscending() ? Order.ASC : Order.DESC;
            orders.add(new OrderSpecifier(direction, path.get(order.getProperty())));
        }
        return orders.toArray(new OrderSpecifier[0]);
    }
}
