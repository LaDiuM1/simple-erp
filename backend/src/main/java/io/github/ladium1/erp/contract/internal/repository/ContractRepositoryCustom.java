package io.github.ladium1.erp.contract.internal.repository;

import io.github.ladium1.erp.contract.internal.dto.ContractSearchCondition;
import io.github.ladium1.erp.contract.internal.entity.Contract;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.List;

public interface ContractRepositoryCustom {

    Page<Contract> search(ContractSearchCondition condition, Pageable pageable);

    List<Contract> searchAll(ContractSearchCondition condition, Sort sort);
}
