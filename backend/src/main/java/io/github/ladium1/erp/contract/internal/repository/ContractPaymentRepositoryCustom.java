package io.github.ladium1.erp.contract.internal.repository;

import java.util.Collection;
import java.util.Map;

public interface ContractPaymentRepositoryCustom {

    /** 계약 ID 별 입금액 합계 — 목록 / 상세의 미수금 자동 산출용. 입금 없는 계약은 키 미포함. */
    Map<Long, Long> sumPaidAmountByContractIds(Collection<Long> contractIds);
}
