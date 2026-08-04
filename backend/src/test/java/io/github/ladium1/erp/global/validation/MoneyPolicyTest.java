package io.github.ladium1.erp.global.validation;

import io.github.ladium1.erp.global.exception.BusinessException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MoneyPolicyTest {

    @Test
    @DisplayName("금액 합계는 JSON 안전 정수 범위까지 보존한다")
    void aggregate_boundary() {
        assertThat(MoneyPolicy.addExact(MoneyPolicy.MAX_TOTAL - 1, 1))
                .isEqualTo(MoneyPolicy.MAX_TOTAL);
        assertThat(MoneyPolicy.fromAggregate(BigDecimal.valueOf(MoneyPolicy.MAX_TOTAL)))
                .isEqualTo(MoneyPolicy.MAX_TOTAL);
    }

    @Test
    @DisplayName("금액 합계가 안전 범위를 넘으면 업무 오류로 거절한다")
    void aggregate_overflow() {
        assertThatThrownBy(() -> MoneyPolicy.addExact(MoneyPolicy.MAX_TOTAL, 1))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(RequestValidationErrorCode.AMOUNT_TOTAL_EXCEEDED);
        assertThatThrownBy(() -> MoneyPolicy.fromAggregate(
                BigDecimal.valueOf(MoneyPolicy.MAX_TOTAL).add(BigDecimal.ONE)))
                .isInstanceOf(BusinessException.class);
    }
}
