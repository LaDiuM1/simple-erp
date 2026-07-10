package io.github.ladium1.erp.expense.internal.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 경비 지출 분류.
 */
@Getter
@RequiredArgsConstructor
public enum ExpenseCategory {

    TRANSPORT("교통비"),
    MEAL("식대"),
    LODGING("숙박비"),
    SUPPLIES("소모품비"),
    ETC("기타");

    private final String label;
}
