package io.github.ladium1.erp.equipment.internal.dto;

import io.github.ladium1.erp.equipment.internal.entity.OutputUnit;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 수동 등록 요청 — 과거 설치분 이관 / 계약 없이 파악된 설비 용.
 * 계약 연결 (contractId) 은 설치완료 이벤트 자동 생성 전용이라 요청에 없다.
 */
public record EquipmentCreateRequest(
        @NotNull
        Long customerId,

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

        @Size(max = 100)
        String serialNo,

        @Size(max = 255)
        String installAddress,

        LocalDate installedDate,

        LocalDate confirmedDate,

        LocalDate warrantyStartDate,

        @PositiveOrZero
        Integer oscillatorWarrantyMonths,

        @PositiveOrZero
        Integer generalWarrantyMonths,

        @NotNull
        Boolean warrantyInsurance,

        String note
) {
}
