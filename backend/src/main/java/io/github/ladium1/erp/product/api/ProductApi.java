package io.github.ladium1.erp.product.api;

import io.github.ladium1.erp.product.api.dto.ProductInfo;

import java.util.List;

public interface ProductApi {

    /**
     * 제품 모델 정보 반환 (카테고리명 포함)
     */
    ProductInfo getById(Long id);

    /**
     * 주어진 id 목록에 해당하는 제품 모델 정보 반환
     */
    List<ProductInfo> findByIds(List<Long> ids);
}
