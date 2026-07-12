package io.github.ladium1.erp.product.internal.service;

import io.github.ladium1.erp.global.audit.AuditAction;
import io.github.ladium1.erp.global.audit.Auditable;
import io.github.ladium1.erp.global.exception.BusinessException;
import io.github.ladium1.erp.global.menu.Menu;
import io.github.ladium1.erp.global.web.PageResponse;
import io.github.ladium1.erp.product.api.ProductApi;
import io.github.ladium1.erp.product.api.ProductDeletingEvent;
import io.github.ladium1.erp.product.api.dto.ProductInfo;
import io.github.ladium1.erp.product.internal.dto.ProductCreateRequest;
import io.github.ladium1.erp.product.internal.dto.ProductDetailResponse;
import io.github.ladium1.erp.product.internal.dto.ProductSearchCondition;
import io.github.ladium1.erp.product.internal.dto.ProductSummaryResponse;
import io.github.ladium1.erp.product.internal.dto.ProductUpdateRequest;
import io.github.ladium1.erp.product.internal.entity.Product;
import io.github.ladium1.erp.product.internal.entity.ProductCategory;
import io.github.ladium1.erp.product.internal.exception.ProductErrorCode;
import io.github.ladium1.erp.product.internal.mapper.ProductMapper;
import io.github.ladium1.erp.product.internal.repository.ProductCategoryRepository;
import io.github.ladium1.erp.product.internal.repository.ProductRepository;
import io.github.ladium1.erp.supplier.api.SupplierApi;
import io.github.ladium1.erp.supplier.api.dto.SupplierInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductService implements ProductApi {

    private final ProductRepository productRepository;
    private final ProductCategoryRepository productCategoryRepository;
    private final ProductMapper productMapper;
    private final SupplierApi supplierApi;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    public ProductInfo getById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ProductErrorCode.PRODUCT_NOT_FOUND));
        return toProductInfo(product);
    }

    @Override
    public List<ProductInfo> findByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        return productRepository.findAllWithCategoryByIds(ids).stream()
                .map(this::toProductInfo)
                .toList();
    }

    public PageResponse<ProductSummaryResponse> search(ProductSearchCondition condition, Pageable pageable) {
        Page<Product> page = productRepository.search(condition, pageable);
        Map<Long, String> supplierNames = loadSupplierNames(page.getContent());
        return PageResponse.of(page.map(
                product -> productMapper.toSummaryResponse(product, supplierNames.get(product.getSupplierId()))
        ));
    }

    public ProductDetailResponse getDetail(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ProductErrorCode.PRODUCT_NOT_FOUND));
        SupplierInfo supplier = supplierApi.getById(product.getSupplierId());
        return productMapper.toDetailResponse(product, supplier.name());
    }

    @Auditable(menu = Menu.PRODUCTS, action = AuditAction.CREATE, targetType = "Product", targetIdFromReturn = true)
    @Transactional
    public Long create(ProductCreateRequest request) {
        // 공급사 존재 검증 — 없으면 supplier 모듈이 SUPPLIER_NOT_FOUND 를 던진다.
        supplierApi.getById(request.supplierId());
        ProductCategory category = resolveCategory(request.categoryId());

        String modelName = request.modelName().trim();
        if (productRepository.existsBySupplierIdAndModelName(request.supplierId(), modelName)) {
            throw new BusinessException(ProductErrorCode.DUPLICATE_MODEL_NAME);
        }

        Product product = Product.builder()
                .category(category)
                .modelName(modelName)
                .supplierId(request.supplierId())
                .note(request.note())
                .active(request.active())
                .build();

        return productRepository.save(product).getId();
    }

    @Auditable(menu = Menu.PRODUCTS, action = AuditAction.UPDATE, targetType = "Product", targetIdParam = "id")
    @Transactional
    public void update(Long id, ProductUpdateRequest request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ProductErrorCode.PRODUCT_NOT_FOUND));
        supplierApi.getById(request.supplierId());
        ProductCategory category = resolveCategory(request.categoryId());

        String modelName = request.modelName().trim();
        if (productRepository.existsBySupplierIdAndModelNameAndIdNot(request.supplierId(), modelName, id)) {
            throw new BusinessException(ProductErrorCode.DUPLICATE_MODEL_NAME);
        }

        product.update(category, modelName, request.supplierId(), request.note(), request.active());
    }

    private ProductCategory resolveCategory(Long categoryId) {
        return productCategoryRepository.findById(categoryId)
                .orElseThrow(() -> new BusinessException(ProductErrorCode.CATEGORY_NOT_FOUND));
    }

    @Auditable(menu = Menu.PRODUCTS, action = AuditAction.DELETE, targetType = "Product", targetIdParam = "id")
    @Transactional
    public void delete(Long id) {
        if (!productRepository.existsById(id)) {
            throw new BusinessException(ProductErrorCode.PRODUCT_NOT_FOUND);
        }
        // 다른 모듈 (계약 등) 의 사용 여부는 동기 이벤트로 검사 — 리스너가 throw 하면 트랜잭션 롤백.
        eventPublisher.publishEvent(new ProductDeletingEvent(id));
        productRepository.deleteById(id);
    }

    /**
     * 일괄 삭제 — 단일 트랜잭션에서 ID 별 단건 delete 호출.
     * 한 건이라도 실패하면 전체 롤백.
     */
    @Auditable(menu = Menu.PRODUCTS, action = AuditAction.DELETE, targetType = "Product")
    @Transactional
    public void deleteAll(List<Long> ids) {
        if (ids == null || ids.isEmpty()) return;
        for (Long id : ids) {
            delete(id);
        }
    }

    private ProductInfo toProductInfo(Product product) {
        return ProductInfo.builder()
                .id(product.getId())
                .categoryId(product.getCategory().getId())
                .categoryName(product.getCategory().getName())
                .modelName(product.getModelName())
                .supplierId(product.getSupplierId())
                .active(product.isActive())
                .build();
    }

    private Map<Long, String> loadSupplierNames(List<Product> products) {
        List<Long> supplierIds = products.stream()
                .map(Product::getSupplierId)
                .distinct()
                .toList();
        return supplierApi.findByIds(supplierIds).stream()
                .collect(Collectors.toMap(SupplierInfo::id, SupplierInfo::name, (a, b) -> a));
    }
}
