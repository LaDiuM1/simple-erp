package io.github.ladium1.erp.product.internal.service;

import io.github.ladium1.erp.global.audit.AuditAction;
import io.github.ladium1.erp.global.audit.Auditable;
import io.github.ladium1.erp.global.exception.BusinessException;
import io.github.ladium1.erp.global.menu.Menu;
import io.github.ladium1.erp.product.internal.dto.ProductCategoryCreateRequest;
import io.github.ladium1.erp.product.internal.dto.ProductCategoryReorderRequest;
import io.github.ladium1.erp.product.internal.dto.ProductCategoryResponse;
import io.github.ladium1.erp.product.internal.dto.ProductCategoryUpdateRequest;
import io.github.ladium1.erp.product.internal.entity.ProductCategory;
import io.github.ladium1.erp.product.internal.exception.ProductErrorCode;
import io.github.ladium1.erp.product.internal.repository.ProductCategoryRepository;
import io.github.ladium1.erp.product.internal.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 제품 카테고리 관리 — 별도 메뉴 없이 제품 모델 관리 (PRODUCTS) 의 서브 기능.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductCategoryService {

    private final ProductCategoryRepository productCategoryRepository;
    private final ProductRepository productRepository;

    public List<ProductCategoryResponse> findAll() {
        Map<Long, Long> productCounts = productRepository.countGroupByCategory();
        return productCategoryRepository.findAllByOrderBySortOrderAsc().stream()
                .map(category -> ProductCategoryResponse.builder()
                        .id(category.getId())
                        .name(category.getName())
                        .sortOrder(category.getSortOrder())
                        .productCount(productCounts.getOrDefault(category.getId(), 0L))
                        .build())
                .toList();
    }

    @Auditable(menu = Menu.PRODUCTS, action = AuditAction.CREATE, targetType = "ProductCategory", targetIdFromReturn = true)
    @Transactional
    public Long create(ProductCategoryCreateRequest request) {
        String name = request.name().trim();
        if (productCategoryRepository.existsByName(name)) {
            throw new BusinessException(ProductErrorCode.DUPLICATE_CATEGORY_NAME);
        }

        int nextOrder = productCategoryRepository.findTopByOrderBySortOrderDesc()
                .map(last -> last.getSortOrder() + 1)
                .orElse(1);

        ProductCategory category = ProductCategory.builder()
                .name(name)
                .sortOrder(nextOrder)
                .build();

        return productCategoryRepository.save(category).getId();
    }

    @Auditable(menu = Menu.PRODUCTS, action = AuditAction.UPDATE, targetType = "ProductCategory", targetIdParam = "id")
    @Transactional
    public void update(Long id, ProductCategoryUpdateRequest request) {
        ProductCategory category = productCategoryRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ProductErrorCode.CATEGORY_NOT_FOUND));

        String name = request.name().trim();
        if (productCategoryRepository.existsByNameAndIdNot(name, id)) {
            throw new BusinessException(ProductErrorCode.DUPLICATE_CATEGORY_NAME);
        }

        category.update(name);
    }

    @Auditable(menu = Menu.PRODUCTS, action = AuditAction.DELETE, targetType = "ProductCategory", targetIdParam = "id")
    @Transactional
    public void delete(Long id) {
        if (!productCategoryRepository.existsById(id)) {
            throw new BusinessException(ProductErrorCode.CATEGORY_NOT_FOUND);
        }
        // 같은 모듈이므로 이벤트 없이 직접 참조 검사.
        if (productRepository.existsByCategoryId(id)) {
            throw new BusinessException(ProductErrorCode.CATEGORY_IN_USE);
        }
        productCategoryRepository.deleteById(id);
    }

    @Auditable(menu = Menu.PRODUCTS, action = AuditAction.UPDATE, targetType = "ProductCategory")
    @Transactional
    public void reorder(ProductCategoryReorderRequest request) {
        List<Long> orderedIds = request.orderedIds();
        if (orderedIds == null || orderedIds.isEmpty()) {
            throw new BusinessException(ProductErrorCode.INVALID_REORDER_PAYLOAD);
        }
        Set<Long> uniqueIds = new HashSet<>(orderedIds);
        if (uniqueIds.size() != orderedIds.size()) {
            throw new BusinessException(ProductErrorCode.INVALID_REORDER_PAYLOAD);
        }

        List<ProductCategory> all = productCategoryRepository.findAll();
        if (all.size() != orderedIds.size()) {
            throw new BusinessException(ProductErrorCode.INVALID_REORDER_PAYLOAD);
        }
        Map<Long, ProductCategory> byId = all.stream()
                .collect(Collectors.toMap(ProductCategory::getId, Function.identity()));
        if (!byId.keySet().equals(uniqueIds)) {
            throw new BusinessException(ProductErrorCode.INVALID_REORDER_PAYLOAD);
        }

        int order = 1;
        for (Long id : orderedIds) {
            byId.get(id).changeSortOrder(order++);
        }
    }
}
