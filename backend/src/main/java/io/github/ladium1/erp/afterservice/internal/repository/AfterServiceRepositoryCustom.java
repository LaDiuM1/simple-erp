package io.github.ladium1.erp.afterservice.internal.repository;

import io.github.ladium1.erp.afterservice.internal.dto.AfterServiceSearchCondition;
import io.github.ladium1.erp.afterservice.internal.entity.AfterService;
import io.github.ladium1.erp.afterservice.internal.entity.ServiceType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface AfterServiceRepositoryCustom {

    Page<AfterService> search(AfterServiceSearchCondition condition, Pageable pageable);

    List<AfterService> searchAll(AfterServiceSearchCondition condition, Sort sort);

    /** 접수일 >= fromDate 인 AS 유형별 건수 — 대시보드 위젯용. 건수 0 유형은 키 미포함. */
    Map<ServiceType, Long> countByTypeSince(LocalDate fromDate);

    /** 접수일 >= fromDate 인 AS 유형별 Σ경비 (경비 → AS 건 join) — 경비 없는 유형은 키 미포함. */
    Map<ServiceType, Long> expenseSumByTypeSince(LocalDate fromDate);

    /** 접수일 >= fromDate 인 AS 의 엔지니어별 Σ경비 — 엔지니어 미지정 경비 제외. */
    Map<Long, Long> expenseSumByEngineerSince(LocalDate fromDate);
}
