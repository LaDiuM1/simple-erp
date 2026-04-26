package io.github.ladium1.erp.salescontact.internal.dto;

public record SalesContactSearchCondition(
        String nameKeyword,
        String emailKeyword,
        String phoneKeyword
) {
}
