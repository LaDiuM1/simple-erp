package io.github.ladium1.erp.salescontact.internal.dto;

import lombok.Builder;

import java.time.LocalDate;
import java.util.List;

@Builder
public record SalesContactSummaryResponse(
        Long id,
        String name,
        String mobilePhone,
        String email,
        String currentCompanyName,
        String currentPosition,
        String currentDepartment,
        LocalDate metAt,
        /** type ASC → name ASC 로 정렬된 컨택 경로 목록. */
        List<AcquisitionSourceInfo> sources
) {
}
