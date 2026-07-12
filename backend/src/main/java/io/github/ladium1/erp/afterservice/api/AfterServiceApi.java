package io.github.ladium1.erp.afterservice.api;

import io.github.ladium1.erp.afterservice.api.dto.EngineerExpenseStat;
import io.github.ladium1.erp.afterservice.api.dto.ServiceTypeStat;

import java.util.List;

public interface AfterServiceApi {

    /**
     * 접수일 기준 최근 N개월 AS 유형별 건수 / 원가 — 대시보드 위젯용.
     */
    List<ServiceTypeStat> typeStats(int months);

    /**
     * 접수일 기준 최근 N개월 엔지니어별 원가 (Σ경비, 내림차순) — 대시보드 위젯용.
     */
    List<EngineerExpenseStat> engineerExpenseStats(int months);
}
