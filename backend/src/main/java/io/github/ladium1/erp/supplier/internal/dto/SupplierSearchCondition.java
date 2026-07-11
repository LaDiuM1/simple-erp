package io.github.ladium1.erp.supplier.internal.dto;

public record SupplierSearchCondition(
        /** 영문 / 한글 표기 통합 키워드 */
        String keyword,

        Boolean active
) {
}
