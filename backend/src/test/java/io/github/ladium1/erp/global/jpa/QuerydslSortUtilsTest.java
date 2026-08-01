package io.github.ladium1.erp.global.jpa;

import io.github.ladium1.erp.customer.internal.entity.QCustomer;
import io.github.ladium1.erp.global.exception.BusinessException;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Sort;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class QuerydslSortUtilsTest {

    @Test
    void acceptsExplicitPublicSortProperties() {
        var orders = QuerydslSortUtils.toOrderSpecifiers(
                Sort.by("id", "roadAddress"),
                Map.of(
                        "id", QCustomer.customer.id,
                        "roadAddress", QCustomer.customer.address.roadAddress
                ),
                QCustomer.customer.id.desc()
        );

        assertThat(orders).hasSize(2);
    }

    @Test
    void rejectsUnknownPropertyAsBadRequestContract() {
        assertThatThrownBy(() -> QuerydslSortUtils.toOrderSpecifiers(
                Sort.by("unknownField"),
                Map.of("id", QCustomer.customer.id),
                QCustomer.customer.id.desc()
        ))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", QueryErrorCode.INVALID_SORT_PROPERTY);
    }

    @Test
    void rejectsInternalCollectionEmbeddedAndCredentialProperties() {
        for (String property : java.util.List.of("steps", "address", "password")) {
            assertThatThrownBy(() -> QuerydslSortUtils.toOrderSpecifiers(
                    Sort.by(property),
                    Map.of("id", QCustomer.customer.id),
                    QCustomer.customer.id.desc()
            ))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", QueryErrorCode.INVALID_SORT_PROPERTY);
        }
    }

    @Test
    void appendsIdTieBreakerForStablePaging() {
        var orders = QuerydslSortUtils.toOrderSpecifiers(
                Sort.by("status"),
                Map.of("id", QCustomer.customer.id, "status", QCustomer.customer.status),
                QCustomer.customer.id.desc()
        );

        assertThat(orders).hasSize(2);
        assertThat(orders[1].getTarget()).isEqualTo(QCustomer.customer.id);
    }
}
