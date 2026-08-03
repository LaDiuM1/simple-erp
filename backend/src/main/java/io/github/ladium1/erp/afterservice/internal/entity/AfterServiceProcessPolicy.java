package io.github.ladium1.erp.afterservice.internal.entity;

import io.github.ladium1.erp.afterservice.internal.exception.AfterServiceErrorCode;
import io.github.ladium1.erp.global.exception.BusinessException;

import java.time.LocalDate;

/** AS 진행 상태, 완료일, 유상 청구액 사이의 불변식을 검증한다. */
public final class AfterServiceProcessPolicy {

    private AfterServiceProcessPolicy() {
    }

    public static void validate(
            ServiceStatus status,
            LocalDate receivedDate,
            LocalDate completedDate,
            WarrantyDecision warrantyDecision,
            Long billingAmount
    ) {
        if (warrantyDecision == WarrantyDecision.PAID && (billingAmount == null || billingAmount <= 0)) {
            throw new BusinessException(AfterServiceErrorCode.PAID_BILLING_AMOUNT_REQUIRED);
        }

        if (status == ServiceStatus.COMPLETED) {
            if (completedDate == null) {
                throw new BusinessException(AfterServiceErrorCode.COMPLETED_DATE_REQUIRED);
            }
            if (completedDate.isBefore(receivedDate)) {
                throw new BusinessException(AfterServiceErrorCode.COMPLETED_DATE_BEFORE_RECEIVED_DATE);
            }
            return;
        }

        if (completedDate != null) {
            throw new BusinessException(AfterServiceErrorCode.COMPLETED_DATE_NOT_ALLOWED);
        }
    }
}
