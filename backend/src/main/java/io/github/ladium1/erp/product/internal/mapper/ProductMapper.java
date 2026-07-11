package io.github.ladium1.erp.product.internal.mapper;

import io.github.ladium1.erp.product.internal.dto.ProductDetailResponse;
import io.github.ladium1.erp.product.internal.dto.ProductSummaryResponse;
import io.github.ladium1.erp.product.internal.entity.Product;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ProductMapper {

    ProductSummaryResponse toSummaryResponse(Product product, String supplierName);

    ProductDetailResponse toDetailResponse(Product product, String supplierName);
}
