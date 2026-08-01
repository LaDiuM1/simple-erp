package io.github.ladium1.erp.contract.internal.dto;

import io.github.ladium1.erp.contract.internal.entity.ContractStatus;
import io.github.ladium1.erp.contract.internal.entity.OutputUnit;
import io.github.ladium1.erp.contract.internal.entity.SupportProgramStatus;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ContractCreateRequest(
        /**
         * 계약 번호 — 채번 규칙의 inputMode 가 AUTO 면 무시되고 시스템이 생성한다.
         * MANUAL / AUTO_OR_MANUAL+직접입력 시 사용자 입력값을 패턴 검증 후 사용.
         */
        @Size(max = 50)
        String contractNo,

        @NotNull
        Long customerId,

        @NotNull
        Long employeeId,

        /**
         * 제품 모델 참조 — 공급사는 요청으로 받지 않고 서버가 제품의 공급사로 저장한다 (불일치 원천 차단).
         */
        @NotNull
        Long productId,

        @PositiveOrZero
        @Digits(integer = 8, fraction = 2)
        @DecimalMax("99999999.99")
        BigDecimal outputValue,

        OutputUnit outputUnit,

        String optionText,

        @PositiveOrZero
        Long initialAmount,

        @NotNull @PositiveOrZero
        Long finalAmount,

        @Size(max = 10)
        String cretopGrade,

        @Size(max = 200)
        String supportProgramName,

        @NotNull
        SupportProgramStatus supportProgramStatus,

        @NotNull
        LocalDate contractDate,

        LocalDate dueDate,

        LocalDate orderDate,

        LocalDate expectedArrivalDate,

        LocalDate arrivalDate,

        LocalDate installedDate,

        LocalDate settledDate,

        @Size(max = 255)
        String logisticsNote,

        @NotNull
        ContractStatus status
) {
}
