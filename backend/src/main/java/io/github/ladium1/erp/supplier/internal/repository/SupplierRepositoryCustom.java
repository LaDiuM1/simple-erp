package io.github.ladium1.erp.supplier.internal.repository;

import io.github.ladium1.erp.supplier.internal.dto.SupplierSearchCondition;
import io.github.ladium1.erp.supplier.internal.entity.Supplier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface SupplierRepositoryCustom {

    Page<Supplier> search(SupplierSearchCondition condition, Pageable pageable);
}
