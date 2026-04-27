package io.github.ladium1.erp.salescontact.internal.dto;

import io.github.ladium1.erp.salescontact.internal.entity.AcquisitionSourceType;
import lombok.Builder;

/**
 * 영업 명부 응답 DTO 가 노출하는 컨택 경로 요약.
 */
@Builder
public record AcquisitionSourceInfo(
        Long id,
        String name,
        AcquisitionSourceType type,
        String description
) {
}
