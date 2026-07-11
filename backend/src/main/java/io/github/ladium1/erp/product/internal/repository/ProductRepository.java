package io.github.ladium1.erp.product.internal.repository;

import io.github.ladium1.erp.product.internal.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long>, ProductRepositoryCustom {

    boolean existsBySupplierIdAndModelName(Long supplierId, String modelName);

    boolean existsBySupplierIdAndModelNameAndIdNot(Long supplierId, String modelName, Long id);

    boolean existsBySupplierId(Long supplierId);

    boolean existsByCategoryId(Long categoryId);
}
