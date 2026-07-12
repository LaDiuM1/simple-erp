package io.github.ladium1.erp.equipment.internal.dto;

import io.github.ladium1.erp.equipment.internal.entity.OutputUnit;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 엑셀 다운로드 전용 행 — 기존 "설비 설치완료 업체리스트" 파일의 컬럼 구성을 미러.
 */
@Builder
public record EquipmentExcelRow(
        String customerName,
        String installAddress,
        String supplierName,
        String categoryName,
        String productModelName,
        BigDecimal outputValue,
        OutputUnit outputUnit,
        String serialNo,
        LocalDate installedDate,
        LocalDate confirmedDate,
        LocalDate warrantyStartDate,
        Integer oscillatorWarrantyMonths,
        LocalDate oscillatorWarrantyEndDate,
        Integer generalWarrantyMonths,
        LocalDate generalWarrantyEndDate,
        boolean warrantyInsurance,
        String contractNo,
        String note
) {
}
