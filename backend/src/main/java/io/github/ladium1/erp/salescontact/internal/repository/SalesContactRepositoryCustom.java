package io.github.ladium1.erp.salescontact.internal.repository;

import io.github.ladium1.erp.salescontact.internal.dto.SalesContactSearchCondition;
import io.github.ladium1.erp.salescontact.internal.entity.SalesContact;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface SalesContactRepositoryCustom {

    Page<SalesContact> search(SalesContactSearchCondition condition, Pageable pageable);
}
