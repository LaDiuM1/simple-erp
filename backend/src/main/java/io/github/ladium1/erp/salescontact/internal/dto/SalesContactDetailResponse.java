package io.github.ladium1.erp.salescontact.internal.dto;

import lombok.Builder;

import java.time.LocalDate;
import java.util.List;

@Builder
public record SalesContactDetailResponse(
        Long id,
        String name,
        String nameEn,
        String mobilePhone,
        String officePhone,
        String email,
        String personalEmail,
        LocalDate metAt,
        String note,
        /** type ASC → name ASC 로 정렬된 컨택 경로 목록. */
        List<AcquisitionSourceInfo> sources,
        List<SalesContactEmploymentResponse> employments
) {
}
