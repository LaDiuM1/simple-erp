package io.github.ladium1.erp.global.validation;

import io.github.ladium1.erp.global.exception.BusinessException;

import java.math.BigDecimal;

/** JSON Number 정밀도와 업무 합계 범위를 함께 보호하는 금액 정책. */
public final class MoneyPolicy {

    public static final long MAX_AMOUNT = 9_999_999_999_999L;
    public static final long MAX_TOTAL = 9_007_199_254_740_991L;

    private MoneyPolicy() {
    }

    public static long addExact(long left, long right) {
        if (left < 0 || right < 0 || left > MAX_TOTAL - right) {
            throw totalExceeded();
        }
        return left + right;
    }

    public static long fromAggregate(BigDecimal total) {
        if (total == null) {
            return 0L;
        }
        try {
            long value = total.longValueExact();
            if (value < 0 || value > MAX_TOTAL) {
                throw totalExceeded();
            }
            return value;
        } catch (ArithmeticException exception) {
            throw totalExceeded();
        }
    }

    private static BusinessException totalExceeded() {
        return new BusinessException(RequestValidationErrorCode.AMOUNT_TOTAL_EXCEEDED);
    }
}
