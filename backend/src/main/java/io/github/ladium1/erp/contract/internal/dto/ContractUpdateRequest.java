package io.github.ladium1.erp.contract.internal.dto;

import io.github.ladium1.erp.contract.internal.entity.ContractStatus;
import io.github.ladium1.erp.contract.internal.entity.OutputUnit;
import io.github.ladium1.erp.contract.internal.entity.SupportProgramStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 계약 수정 요청 — 계약 번호는 발급 후 불변이라 수정 항목에 없다.
 */
public record ContractUpdateRequest(
        @NotNull
        Long customerId,

        @NotNull
        Long employeeId,

        @NotNull
        Long productId,

        @PositiveOrZero
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
