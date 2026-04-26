package io.github.ladium1.erp.salescontact.api.dto;

import lombok.Builder;

/**
 * 다른 모듈이 참조하는 영업 명부 요약 정보. 활성 재직(Employment) 의 회사 / 직책 캐시 포함.
 */
@Builder
public record SalesContactInfo(
        Long id,
        String name,
        String currentCompanyName,
        String currentPosition
) {
}
