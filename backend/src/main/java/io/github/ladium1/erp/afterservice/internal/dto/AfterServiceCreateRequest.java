package io.github.ladium1.erp.afterservice.internal.dto;

import io.github.ladium1.erp.afterservice.internal.entity.ServiceStatus;
import io.github.ladium1.erp.afterservice.internal.entity.ServiceType;
import io.github.ladium1.erp.afterservice.internal.entity.WarrantyDecision;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record AfterServiceCreateRequest(
        /**
         * AS 접수번호 — 채번 규칙의 inputMode 가 AUTO 면 무시되고 시스템이 생성한다.
         * MANUAL / AUTO_OR_MANUAL+직접입력 시 사용자 입력값을 패턴 검증 후 사용.
         */
        @Size(max = 50)
        String receiptNo,

        @NotNull
        Long customerId,

        /**
         * 설비 대장 참조 — 대장 미등록 설비 접수는 null 허용 (등록 후 연결 보완 가능).
         */
        Long equipmentId,

        @NotNull
        LocalDate receivedDate,

        @NotNull
        ServiceType type,

        String symptom,

        @NotNull
        ServiceStatus status,

        Long assignedEngineerId,

        @NotNull
        WarrantyDecision warrantyDecision,

        @PositiveOrZero
        Long billingAmount,

        LocalDate completedDate
) {
}
