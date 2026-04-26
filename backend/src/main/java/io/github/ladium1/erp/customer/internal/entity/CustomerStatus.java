package io.github.ladium1.erp.customer.internal.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CustomerStatus {

    ACTIVE("거래중"),
    INACTIVE("비거래"),
    SUSPENDED("거래중지");

    private final String description;
}
