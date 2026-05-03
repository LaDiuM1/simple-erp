package io.github.ladium1.erp.customer.internal.dto;

import io.github.ladium1.erp.customer.internal.entity.CustomerStatus;
import io.github.ladium1.erp.customer.internal.entity.CustomerType;

import java.util.Set;

public record CustomerSearchCondition(
        String codeKeyword,
        String nameKeyword,
        String addressKeyword,
        String phoneKeyword,
        CustomerType type,
        CustomerStatus status,
        /**
         * 데이터 스코프로 좁힌 가시 customer ID 집합. null = 제한 없음 (ALL).
         * Controller 가 보내지 않고 service 가 합성.
         */
        Set<Long> idScope
) {
    /**
     * Controller 호환 — idScope 미지정 (ALL) 으로 시작.
     */
    public CustomerSearchCondition(String codeKeyword, String nameKeyword, String addressKeyword,
                                   String phoneKeyword, CustomerType type, CustomerStatus status) {
        this(codeKeyword, nameKeyword, addressKeyword, phoneKeyword, type, status, null);
    }

    public CustomerSearchCondition withIdScope(Set<Long> idScope) {
        return new CustomerSearchCondition(codeKeyword, nameKeyword, addressKeyword, phoneKeyword, type, status, idScope);
    }
}
