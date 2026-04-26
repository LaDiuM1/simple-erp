package io.github.ladium1.erp.customer.internal.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CustomerType {

    POTENTIAL("잠재고객"),
    GENERAL("일반고객"),
    KEY_ACCOUNT("주요고객"),
    PARTNER("파트너");

    private final String description;
}
