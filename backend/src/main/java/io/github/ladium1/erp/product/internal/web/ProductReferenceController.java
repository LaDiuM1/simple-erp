package io.github.ladium1.erp.product.internal.web;

import io.github.ladium1.erp.global.web.PageResponse;
import io.github.ladium1.erp.product.internal.dto.ProductCategoryReferenceResponse;
import io.github.ladium1.erp.product.internal.dto.ProductReferenceResponse;
import io.github.ladium1.erp.product.internal.dto.ProductSearchCondition;
import io.github.ladium1.erp.product.internal.service.ProductCategoryService;
import io.github.ladium1.erp.product.internal.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/products/reference")
@RequiredArgsConstructor
public class ProductReferenceController {

    private static final String CAN_READ_REFERENCE =
            "@menuPermissionEvaluator.canRead(authentication, 'PRODUCTS') "
            + "or @menuPermissionEvaluator.canRead(authentication, 'CONTRACTS') "
            + "or @menuPermissionEvaluator.canRead(authentication, 'EQUIPMENTS')";

    private final ProductService productService;
    private final ProductCategoryService productCategoryService;

    @GetMapping
    @PreAuthorize(CAN_READ_REFERENCE)
    public PageResponse<ProductReferenceResponse> search(
            @RequestParam(required = false) String modelNameKeyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long supplierId,
            @RequestParam(required = false) Boolean active,
            @PageableDefault(sort = "modelName", direction = Sort.Direction.ASC) Pageable pageable
    ) {
        return productService.searchReference(
                new ProductSearchCondition(modelNameKeyword, categoryId, supplierId, active),
                pageable
        );
    }

    @GetMapping("/categories")
    @PreAuthorize(CAN_READ_REFERENCE)
    public List<ProductCategoryReferenceResponse> findCategories() {
        return productCategoryService.findReferences();
    }

    @GetMapping("/{id}")
    @PreAuthorize(CAN_READ_REFERENCE)
    public ProductReferenceResponse getReference(@PathVariable Long id) {
        return productService.getReference(id);
    }
}
