package io.github.ladium1.erp.afterservice.internal.repository;

import io.github.ladium1.erp.afterservice.internal.dto.AfterServiceSearchCondition;
import io.github.ladium1.erp.afterservice.internal.entity.AfterService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.List;

public interface AfterServiceRepositoryCustom {

    Page<AfterService> search(AfterServiceSearchCondition condition, Pageable pageable);

    List<AfterService> searchAll(AfterServiceSearchCondition condition, Sort sort);
}
