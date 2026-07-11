package io.github.ladium1.erp.product.internal.repository;

import io.github.ladium1.erp.product.internal.dto.ProductSearchCondition;
import io.github.ladium1.erp.product.internal.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProductRepositoryCustom {

    Page<Product> search(ProductSearchCondition condition, Pageable pageable);
}
