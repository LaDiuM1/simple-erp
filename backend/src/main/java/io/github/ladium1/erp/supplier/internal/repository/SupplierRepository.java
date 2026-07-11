package io.github.ladium1.erp.supplier.internal.repository;

import io.github.ladium1.erp.supplier.internal.entity.Supplier;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SupplierRepository extends JpaRepository<Supplier, Long>, SupplierRepositoryCustom {

    boolean existsByName(String name);

    boolean existsByNameAndIdNot(String name, Long id);
}
