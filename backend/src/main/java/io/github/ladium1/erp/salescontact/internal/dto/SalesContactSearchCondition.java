package io.github.ladium1.erp.salescontact.internal.dto;

import java.util.List;

public record SalesContactSearchCondition(
        String nameKeyword,
        String emailKeyword,
        String phoneKeyword,
        List<Long> sourceIds
) {
}
