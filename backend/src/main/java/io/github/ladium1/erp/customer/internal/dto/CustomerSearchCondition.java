package io.github.ladium1.erp.customer.internal.dto;

import io.github.ladium1.erp.customer.internal.entity.CustomerStatus;
import io.github.ladium1.erp.customer.internal.entity.CustomerType;

public record CustomerSearchCondition(
        String codeKeyword,
        String nameKeyword,
        String addressKeyword,
        String phoneKeyword,
        CustomerType type,
        CustomerStatus status
) {
}
