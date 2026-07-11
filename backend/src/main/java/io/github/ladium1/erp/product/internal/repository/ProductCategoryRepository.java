package io.github.ladium1.erp.product.internal.repository;

import io.github.ladium1.erp.product.internal.entity.ProductCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProductCategoryRepository extends JpaRepository<ProductCategory, Long> {

    List<ProductCategory> findAllByOrderBySortOrderAsc();

    Optional<ProductCategory> findTopByOrderBySortOrderDesc();

    boolean existsByName(String name);

    boolean existsByNameAndIdNot(String name, Long id);
}
