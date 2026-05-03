package io.github.ladium1.erp.customer.api;

import io.github.ladium1.erp.global.security.DataScope;
import io.github.ladium1.erp.global.security.DataScopeContext;

import java.util.Set;

/**
 * 데이터 스코프 적용 시 customer 모듈이 외부에서 받아오는 가시성 SPI.
 * <p>
 * 다른 모듈 (예: salescustomer) 이 자기 도메인 정보 (담당자 배정 등) 를 근거로
 * 사용자가 볼 수 있는 customer 식별자 집합을 기여한다. customer 서비스가
 * 등록된 모든 contributor 의 결과를 UNION 해 검색 / 단건 조회 / 수정 / 삭제 가시성에 적용.
 *
 * <p>비즈니스 규칙:
 * - {@link DataScope#ALL} 은 호출되지 않음 (서비스가 분기로 통과)
 * - 결과 빈 집합 = 이 contributor 가 책임지는 행 없음 (다른 contributor 가 기여 가능)
 * - contributor 가 하나도 등록되지 않은 상태에서 ALL 외 스코프가 걸리면 service 가 빈 결과 반환
 */
public interface CustomerVisibilityContributor {

    Set<Long> visibleCustomerIds(DataScope scope, DataScopeContext context);
}
