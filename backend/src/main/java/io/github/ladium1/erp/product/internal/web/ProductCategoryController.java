package io.github.ladium1.erp.product.internal.web;

import io.github.ladium1.erp.product.internal.dto.ProductCategoryCreateRequest;
import io.github.ladium1.erp.product.internal.dto.ProductCategoryReorderRequest;
import io.github.ladium1.erp.product.internal.dto.ProductCategoryResponse;
import io.github.ladium1.erp.product.internal.dto.ProductCategoryUpdateRequest;
import io.github.ladium1.erp.product.internal.service.ProductCategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 제품 카테고리 관리 — 별도 메뉴 없이 제품 모델 관리 (PRODUCTS) 권한을 그대로 사용하는 서브 기능.
 */
@RestController
@RequestMapping("/api/v1/products/categories")
@RequiredArgsConstructor
public class ProductCategoryController {

    private static final String MENU_CODE = "PRODUCTS";
    private static final String CAN_READ = "@menuPermissionEvaluator.canRead(authentication, '" + MENU_CODE + "')";
    private static final String CAN_WRITE = "@menuPermissionEvaluator.canWrite(authentication, '" + MENU_CODE + "')";

    private final ProductCategoryService productCategoryService;

    @GetMapping
    @PreAuthorize(CAN_READ)
    public List<ProductCategoryResponse> findAll() {
        return productCategoryService.findAll();
    }

    @PostMapping
    @PreAuthorize(CAN_WRITE)
    public Long create(@Valid @RequestBody ProductCategoryCreateRequest request) {
        return productCategoryService.create(request);
    }

    @PutMapping("/reorder")
    @PreAuthorize(CAN_WRITE)
    public void reorder(@Valid @RequestBody ProductCategoryReorderRequest request) {
        productCategoryService.reorder(request);
    }

    @PutMapping("/{id}")
    @PreAuthorize(CAN_WRITE)
    public void update(@PathVariable Long id, @Valid @RequestBody ProductCategoryUpdateRequest request) {
        productCategoryService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize(CAN_WRITE)
    public void delete(@PathVariable Long id) {
        productCategoryService.delete(id);
    }
}
