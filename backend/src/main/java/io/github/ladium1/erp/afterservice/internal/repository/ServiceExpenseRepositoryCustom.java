package io.github.ladium1.erp.afterservice.internal.repository;

import java.util.Collection;
import java.util.Map;

public interface ServiceExpenseRepositoryCustom {

    /** AS 건 ID 별 경비 합계 — 목록 / 상세의 원가 표시용. 경비 없는 건은 키 미포함. */
    Map<Long, Long> sumAmountByAfterServiceIds(Collection<Long> afterServiceIds);
}
