package io.github.ladium1.erp.salescontact.internal.repository;

import io.github.ladium1.erp.salescontact.internal.dto.SalesContactSearchCondition;
import io.github.ladium1.erp.salescontact.internal.entity.SalesContact;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.List;

public interface SalesContactRepositoryCustom {

    Page<SalesContact> search(SalesContactSearchCondition condition, Pageable pageable);

    /**
     * 페이징 없이 전체 조회 — 엑셀 다운로드 용. 검색 조건은 search 와 동일하게 적용.
     */
    List<SalesContact> searchAll(SalesContactSearchCondition condition, Sort sort);
}
