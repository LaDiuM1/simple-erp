package io.github.ladium1.erp.contract.api;

import io.github.ladium1.erp.contract.api.dto.ContractInfo;
import io.github.ladium1.erp.contract.api.dto.ContractOutstandingSummary;
import io.github.ladium1.erp.contract.api.dto.MonthlyContractStat;

import java.util.List;

public interface ContractApi {

    /**
     * 계약 정보 반환
     */
    ContractInfo getById(Long id);

    /**
     * 주어진 id 목록에 해당하는 계약 정보 반환 — 설비 대장 등의 계약번호 표시용
     */
    List<ContractInfo> findByIds(List<Long> ids);

    /**
     * 최근 N개월 월별 계약 건수 / 금액 — 대시보드 위젯용.
     * 계약취소 제외, 빈 달도 0 으로 채워 반환. 호출자의 데이터 스코프 적용.
     */
    List<MonthlyContractStat> monthlyStats(int months);

    /**
     * 수금 vs 미수 누적 현황 — 대시보드 위젯용. 계약취소 제외, 호출자의 데이터 스코프 적용.
     */
    ContractOutstandingSummary outstandingSummary();
}
