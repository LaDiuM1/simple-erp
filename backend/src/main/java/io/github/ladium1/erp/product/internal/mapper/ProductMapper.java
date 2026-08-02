package io.github.ladium1.erp.product.internal.mapper;

import io.github.ladium1.erp.product.internal.dto.ProductDetailResponse;
import io.github.ladium1.erp.product.internal.dto.ProductReferenceResponse;
import io.github.ladium1.erp.product.internal.dto.ProductSummaryResponse;
import io.github.ladium1.erp.product.internal.entity.Product;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ProductMapper {

    @Mapping(source = "product.category.id", target = "categoryId")
    @Mapping(source = "product.category.name", target = "categoryName")
    ProductSummaryResponse toSummaryResponse(Product product, String supplierName);

    @Mapping(source = "product.category.name", target = "categoryName")
    ProductReferenceResponse toReferenceResponse(Product product, String supplierName);

    @Mapping(source = "product.category.id", target = "categoryId")
    @Mapping(source = "product.category.name", target = "categoryName")
    ProductDetailResponse toDetailResponse(Product product, String supplierName);
}
