package io.github.ladium1.erp.global.validation;

import io.github.ladium1.erp.global.exception.BusinessException;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RequestCollectionPolicyTest {

    @Test
    void mutation_batch_accepts_limit_and_rejects_the_next_item() {
        List<Integer> atLimit = IntStream.range(0, RequestCollectionPolicy.MAX_MUTATION_BATCH_SIZE)
                .boxed()
                .toList();

        assertThatCode(() -> RequestCollectionPolicy.requireBoundedMutationBatch(atLimit))
                .doesNotThrowAnyException();
        assertThatThrownBy(() -> RequestCollectionPolicy.requireBoundedMutationBatch(
                IntStream.rangeClosed(0, RequestCollectionPolicy.MAX_MUTATION_BATCH_SIZE).boxed().toList()))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", RequestValidationErrorCode.INVALID_MUTATION_BATCH);
    }

    @Test
    void mutation_batch_rejects_null_elements_before_domain_iteration() {
        assertThatThrownBy(() -> RequestCollectionPolicy.requireBoundedMutationBatch(Arrays.asList(1L, null)))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", RequestValidationErrorCode.INVALID_MUTATION_BATCH);
    }

    @Test
    void full_reorder_requires_non_empty_non_null_bounded_snapshot() {
        assertThat(RequestCollectionPolicy.isBoundedFullReorder(List.of(1L))).isTrue();
        assertThat(RequestCollectionPolicy.isBoundedFullReorder(List.of())).isFalse();
        assertThat(RequestCollectionPolicy.isBoundedFullReorder(Arrays.asList(1L, null))).isFalse();
        assertThat(RequestCollectionPolicy.isBoundedFullReorder(
                IntStream.rangeClosed(0, RequestCollectionPolicy.MAX_FULL_REORDER_SIZE).boxed().toList()))
                .isFalse();
    }
}
