package io.github.ladium1.erp.product.internal.service;

import io.github.ladium1.erp.global.audit.AuditAction;
import io.github.ladium1.erp.global.audit.Auditable;
import io.github.ladium1.erp.global.exception.BusinessException;
import io.github.ladium1.erp.global.menu.Menu;
import io.github.ladium1.erp.global.web.PageResponse;
import io.github.ladium1.erp.product.internal.dto.ProductCreateRequest;
import io.github.ladium1.erp.product.internal.dto.ProductDetailResponse;
import io.github.ladium1.erp.product.internal.dto.ProductSearchCondition;
import io.github.ladium1.erp.product.internal.dto.ProductSummaryResponse;
import io.github.ladium1.erp.product.internal.dto.ProductUpdateRequest;
import io.github.ladium1.erp.product.internal.entity.Product;
import io.github.ladium1.erp.product.internal.exception.ProductErrorCode;
import io.github.ladium1.erp.product.internal.mapper.ProductMapper;
import io.github.ladium1.erp.product.internal.repository.ProductRepository;
import io.github.ladium1.erp.supplier.api.SupplierApi;
import io.github.ladium1.erp.supplier.api.dto.SupplierInfo;
import lombok.RequiredArgsConstructor;
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
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;
    private final SupplierApi supplierApi;

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

        String modelName = request.modelName().trim();
        if (productRepository.existsBySupplierIdAndModelName(request.supplierId(), modelName)) {
            throw new BusinessException(ProductErrorCode.DUPLICATE_MODEL_NAME);
        }

        Product product = Product.builder()
                .category(request.category())
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

        String modelName = request.modelName().trim();
        if (productRepository.existsBySupplierIdAndModelNameAndIdNot(request.supplierId(), modelName, id)) {
            throw new BusinessException(ProductErrorCode.DUPLICATE_MODEL_NAME);
        }

        product.update(request.category(), modelName, request.supplierId(), request.note(), request.active());
    }

    @Auditable(menu = Menu.PRODUCTS, action = AuditAction.DELETE, targetType = "Product", targetIdParam = "id")
    @Transactional
    public void delete(Long id) {
        if (!productRepository.existsById(id)) {
            throw new BusinessException(ProductErrorCode.PRODUCT_NOT_FOUND);
        }
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

    private Map<Long, String> loadSupplierNames(List<Product> products) {
        List<Long> supplierIds = products.stream()
                .map(Product::getSupplierId)
                .distinct()
                .toList();
        return supplierApi.findByIds(supplierIds).stream()
                .collect(Collectors.toMap(SupplierInfo::id, SupplierInfo::name, (a, b) -> a));
    }
}
