package io.github.ladium1.erp.salescontact.internal.dto;

import lombok.Builder;

import java.time.LocalDate;

/**
 * 엑셀 다운로드 한 행 — 명부 자체 + 활성 재직 캐시 + 만난 경로 콤마 결합.
 * 다른 모듈에 노출되지 않으므로 internal/dto 에 보관.
 */
@Builder
public record SalesContactExcelRow(
        String name,
        String nameEn,
        String mobilePhone,
        String officePhone,
        String email,
        String personalEmail,
        LocalDate metAt,
        String sources,
        String currentCompanyName,
        String currentPosition,
        String currentDepartment,
        String note
) {
}
