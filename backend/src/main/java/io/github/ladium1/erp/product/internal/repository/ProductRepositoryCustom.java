package io.github.ladium1.erp.product.internal.repository;

import io.github.ladium1.erp.product.internal.dto.ProductSearchCondition;
import io.github.ladium1.erp.product.internal.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Map;

public interface ProductRepositoryCustom {

    Page<Product> search(ProductSearchCondition condition, Pageable pageable);

    /** 카테고리 ID 별 제품 모델 수 — 카테고리 관리 화면의 참조 수 표시용 */
    Map<Long, Long> countGroupByCategory();
}
