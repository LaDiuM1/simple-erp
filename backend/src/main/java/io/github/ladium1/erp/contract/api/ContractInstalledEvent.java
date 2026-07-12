package io.github.ladium1.erp.contract.api;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 계약 상태가 설치완료 (INSTALLED) 로 전이될 때 발행되는 이벤트.
 * <p>
 * equipment 모듈이 {@code @ApplicationModuleListener} 로 수신해 설비 대장을 자동 생성한다 —
 * contract 는 equipment 를 모른다 (Modulith 모듈 간 단방향 유지).
 * outputUnit 은 모듈 내부 enum 을 노출하지 않기 위해 name 문자열로 전달.
 */
public record ContractInstalledEvent(
        Long contractId,
        Long customerId,
        Long supplierId,
        Long productId,
        BigDecimal outputValue,
        String outputUnit,
        LocalDate installedDate
) {
}
