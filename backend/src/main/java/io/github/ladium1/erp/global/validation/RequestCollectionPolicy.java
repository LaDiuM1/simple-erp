package io.github.ladium1.erp.global.validation;

import io.github.ladium1.erp.global.exception.BusinessException;

import java.util.Collection;
import java.util.Objects;

/** 요청 하나가 반복 DB 작업으로 증폭되지 않도록 컬렉션 변경의 공통 상한을 고정한다. */
public final class RequestCollectionPolicy {

    public static final int MAX_MUTATION_BATCH_SIZE = 20;
    public static final int MAX_FULL_REORDER_SIZE = 50;

    private RequestCollectionPolicy() {
    }

    public static void requireBoundedMutationBatch(Collection<?> items) {
        if (items == null || items.isEmpty()) {
            return;
        }
        if (items.size() > MAX_MUTATION_BATCH_SIZE || items.stream().anyMatch(Objects::isNull)) {
            throw new BusinessException(RequestValidationErrorCode.INVALID_MUTATION_BATCH);
        }
    }

    public static boolean isBoundedFullReorder(Collection<?> items) {
        return items != null
                && !items.isEmpty()
                && items.size() <= MAX_FULL_REORDER_SIZE
                && items.stream().noneMatch(Objects::isNull);
    }
}
