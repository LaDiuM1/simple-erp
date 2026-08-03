package io.github.ladium1.erp.afterservice.internal.entity;

import io.github.ladium1.erp.afterservice.internal.exception.AfterServiceErrorCode;
import io.github.ladium1.erp.global.exception.BusinessException;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AfterServiceProcessPolicyTest {

    private static final LocalDate RECEIVED_DATE = LocalDate.of(2026, 8, 1);

    @Test
    void paidServiceRequiresPositiveBillingAmount() {
        assertBusinessError(
                () -> validate(ServiceStatus.RECEIVED, null, WarrantyDecision.PAID, null),
                AfterServiceErrorCode.PAID_BILLING_AMOUNT_REQUIRED
        );
        assertBusinessError(
                () -> validate(ServiceStatus.RECEIVED, null, WarrantyDecision.PAID, 0L),
                AfterServiceErrorCode.PAID_BILLING_AMOUNT_REQUIRED
        );
        assertThatCode(() -> validate(ServiceStatus.RECEIVED, null, WarrantyDecision.PAID, 1L))
                .doesNotThrowAnyException();
    }

    @Test
    void completedStatusRequiresDateOnOrAfterReceipt() {
        assertBusinessError(
                () -> validate(ServiceStatus.COMPLETED, null, WarrantyDecision.FREE, null),
                AfterServiceErrorCode.COMPLETED_DATE_REQUIRED
        );
        assertBusinessError(
                () -> validate(
                        ServiceStatus.COMPLETED,
                        RECEIVED_DATE.minusDays(1),
                        WarrantyDecision.FREE,
                        null
                ),
                AfterServiceErrorCode.COMPLETED_DATE_BEFORE_RECEIVED_DATE
        );
        assertThatCode(() -> validate(
                ServiceStatus.COMPLETED,
                RECEIVED_DATE,
                WarrantyDecision.FREE,
                null
        )).doesNotThrowAnyException();
    }

    @Test
    void incompleteStatusRejectsCompletedDate() {
        assertBusinessError(
                () -> validate(ServiceStatus.IN_PROGRESS, RECEIVED_DATE, WarrantyDecision.FREE, null),
                AfterServiceErrorCode.COMPLETED_DATE_NOT_ALLOWED
        );
    }

    private static void validate(
            ServiceStatus status,
            LocalDate completedDate,
            WarrantyDecision decision,
            Long billingAmount
    ) {
        AfterServiceProcessPolicy.validate(status, RECEIVED_DATE, completedDate, decision, billingAmount);
    }

    private static void assertBusinessError(Runnable action, AfterServiceErrorCode errorCode) {
        assertThatThrownBy(action::run)
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", errorCode);
    }
}
