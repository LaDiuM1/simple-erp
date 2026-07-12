package io.github.ladium1.erp.product.internal.service;

import io.github.ladium1.erp.global.exception.BusinessException;
import io.github.ladium1.erp.global.web.PageResponse;
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
import io.github.ladium1.erp.supplier.internal.exception.SupplierErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @InjectMocks
    private ProductService productService;

    @Mock private ProductRepository productRepository;
    @Mock private ProductCategoryRepository productCategoryRepository;
    @Mock private ProductMapper productMapper;
    @Mock private SupplierApi supplierApi;
    @Mock private ApplicationEventPublisher eventPublisher;

    @Test
    @DisplayName("search 성공 — 공급사명 enrich 된 Summary 페이지 반환")
    void search_success() {
        // given
        Product product = mockProduct("HLA-1530", 1L);
        Pageable pageable = PageRequest.of(0, 20);
        given(productRepository.search(any(ProductSearchCondition.class), eq(pageable)))
                .willReturn(new PageImpl<>(List.of(product), pageable, 1));
        given(supplierApi.findByIds(List.of(1L)))
                .willReturn(List.of(supplierInfo(1L, "YAWEI")));
        ProductSummaryResponse summary = ProductSummaryResponse.builder()
                .id(1L).categoryId(1L).categoryName("평판 레이저").modelName("HLA-1530")
                .supplierId(1L).supplierName("YAWEI").active(true).build();
        given(productMapper.toSummaryResponse(product, "YAWEI")).willReturn(summary);

        // when
        PageResponse<ProductSummaryResponse> page =
                productService.search(new ProductSearchCondition(null, null, null, null), pageable);

        // then
        assertThat(page.content()).containsExactly(summary);
    }

    @Test
    @DisplayName("getDetail 성공 — 공급사명 포함")
    void get_detail_success() {
        // given
        Product product = mockProduct("HLA-1530", 1L);
        given(productRepository.findById(1L)).willReturn(Optional.of(product));
        given(supplierApi.getById(1L)).willReturn(supplierInfo(1L, "YAWEI"));
        ProductDetailResponse detail = ProductDetailResponse.builder()
                .id(1L).categoryId(1L).categoryName("평판 레이저").modelName("HLA-1530")
                .supplierId(1L).supplierName("YAWEI").active(true).build();
        given(productMapper.toDetailResponse(product, "YAWEI")).willReturn(detail);

        // when
        ProductDetailResponse actual = productService.getDetail(1L);

        // then
        assertThat(actual).isEqualTo(detail);
    }

    @Test
    @DisplayName("getDetail 실패 — PRODUCT_NOT_FOUND")
    void get_detail_fail_not_found() {
        // given
        given(productRepository.findById(99L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> productService.getDetail(99L))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ProductErrorCode.PRODUCT_NOT_FOUND);
    }

    @Test
    @DisplayName("create 성공 — 공급사 / 카테고리 존재 검증 + 모델명 trim 후 저장")
    void create_success() {
        // given
        ProductCreateRequest request = new ProductCreateRequest(1L, " HLA-1530 ", 1L, null, true);
        given(supplierApi.getById(1L)).willReturn(supplierInfo(1L, "YAWEI"));
        given(productCategoryRepository.findById(1L)).willReturn(Optional.of(mockCategory("평판 레이저", 1)));
        given(productRepository.existsBySupplierIdAndModelName(1L, "HLA-1530")).willReturn(false);
        Product saved = mockProduct("HLA-1530", 1L);
        ReflectionTestUtils.setField(saved, "id", 10L);
        given(productRepository.save(any(Product.class))).willReturn(saved);

        // when
        Long id = productService.create(request);

        // then
        assertThat(id).isEqualTo(10L);
    }

    @Test
    @DisplayName("create 실패 — 존재하지 않는 공급사면 supplier 모듈 예외 전파")
    void create_fail_supplier_not_found() {
        // given
        ProductCreateRequest request = new ProductCreateRequest(1L, "HLA-1530", 99L, null, true);
        given(supplierApi.getById(99L))
                .willThrow(new BusinessException(SupplierErrorCode.SUPPLIER_NOT_FOUND));

        // when & then
        assertThatThrownBy(() -> productService.create(request))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", SupplierErrorCode.SUPPLIER_NOT_FOUND);
        verify(productRepository, never()).save(any());
    }

    @Test
    @DisplayName("create 실패 — 존재하지 않는 카테고리 시 CATEGORY_NOT_FOUND")
    void create_fail_category_not_found() {
        // given
        ProductCreateRequest request = new ProductCreateRequest(99L, "HLA-1530", 1L, null, true);
        given(supplierApi.getById(1L)).willReturn(supplierInfo(1L, "YAWEI"));
        given(productCategoryRepository.findById(99L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> productService.create(request))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ProductErrorCode.CATEGORY_NOT_FOUND);
        verify(productRepository, never()).save(any());
    }

    @Test
    @DisplayName("create 실패 — 같은 공급사에 중복 모델명 시 DUPLICATE_MODEL_NAME")
    void create_fail_duplicate_model_name() {
        // given
        ProductCreateRequest request = new ProductCreateRequest(1L, "HLA-1530", 1L, null, true);
        given(supplierApi.getById(1L)).willReturn(supplierInfo(1L, "YAWEI"));
        given(productCategoryRepository.findById(1L)).willReturn(Optional.of(mockCategory("평판 레이저", 1)));
        given(productRepository.existsBySupplierIdAndModelName(1L, "HLA-1530")).willReturn(true);

        // when & then
        assertThatThrownBy(() -> productService.create(request))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ProductErrorCode.DUPLICATE_MODEL_NAME);
        verify(productRepository, never()).save(any());
    }

    @Test
    @DisplayName("update 성공 — 엔티티의 update 호출")
    void update_success() {
        // given
        Product product = mockProduct("HLA-1530", 1L);
        ProductCategory pipeCategory = mockCategory("파이프 레이저", 3);
        given(productRepository.findById(1L)).willReturn(Optional.of(product));
        given(supplierApi.getById(2L)).willReturn(supplierInfo(2L, "ACME"));
        given(productCategoryRepository.findById(3L)).willReturn(Optional.of(pipeCategory));
        given(productRepository.existsBySupplierIdAndModelNameAndIdNot(2L, "DAP-3S-360", 1L)).willReturn(false);
        ProductUpdateRequest request = new ProductUpdateRequest(3L, "DAP-3S-360", 2L, "비고", false);

        // when
        productService.update(1L, request);

        // then
        assertThat(product.getCategory()).isEqualTo(pipeCategory);
        assertThat(product.getModelName()).isEqualTo("DAP-3S-360");
        assertThat(product.getSupplierId()).isEqualTo(2L);
        assertThat(product.isActive()).isFalse();
    }

    @Test
    @DisplayName("update 실패 — 존재하지 않는 제품 모델")
    void update_fail_not_found() {
        // given
        given(productRepository.findById(99L)).willReturn(Optional.empty());
        ProductUpdateRequest request = new ProductUpdateRequest(1L, "HLA-1530", 1L, null, true);

        // when & then
        assertThatThrownBy(() -> productService.update(99L, request))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ProductErrorCode.PRODUCT_NOT_FOUND);
    }

    @Test
    @DisplayName("delete 성공 — 삭제 전 ProductDeletingEvent 발행")
    void delete_success() {
        // given
        given(productRepository.existsById(1L)).willReturn(true);

        // when
        productService.delete(1L);

        // then
        verify(eventPublisher).publishEvent(new ProductDeletingEvent(1L));
        verify(productRepository).deleteById(1L);
    }

    @Test
    @DisplayName("delete 실패 — 존재하지 않는 제품 모델")
    void delete_fail_not_found() {
        // given
        given(productRepository.existsById(99L)).willReturn(false);

        // when & then
        assertThatThrownBy(() -> productService.delete(99L))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ProductErrorCode.PRODUCT_NOT_FOUND);
        verify(productRepository, never()).deleteById(any());
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    @DisplayName("getById 성공 — 카테고리명 포함 ProductInfo 반환")
    void get_by_id_success() {
        // given
        Product product = mockProduct("HLA-1530", 1L);
        ReflectionTestUtils.setField(product, "id", 7L);
        given(productRepository.findById(7L)).willReturn(Optional.of(product));

        // when
        ProductInfo info = productService.getById(7L);

        // then
        assertThat(info.id()).isEqualTo(7L);
        assertThat(info.modelName()).isEqualTo("HLA-1530");
        assertThat(info.categoryName()).isEqualTo("평판 레이저");
        assertThat(info.supplierId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("getById 실패 — PRODUCT_NOT_FOUND")
    void get_by_id_fail_not_found() {
        // given
        given(productRepository.findById(99L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> productService.getById(99L))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ProductErrorCode.PRODUCT_NOT_FOUND);
    }

    @Test
    @DisplayName("findByIds — 빈 입력은 빈 리스트 (DB 미조회)")
    void find_by_ids_empty_input() {
        assertThat(productService.findByIds(List.of())).isEmpty();
        assertThat(productService.findByIds(null)).isEmpty();
        verify(productRepository, never()).findAllWithCategoryByIds(any());
    }

    private Product mockProduct(String modelName, Long supplierId) {
        return Product.builder()
                .category(mockCategory("평판 레이저", 1))
                .modelName(modelName)
                .supplierId(supplierId)
                .active(true)
                .build();
    }

    private ProductCategory mockCategory(String name, int sortOrder) {
        return ProductCategory.builder()
                .name(name)
                .sortOrder(sortOrder)
                .build();
    }

    private SupplierInfo supplierInfo(Long id, String name) {
        return SupplierInfo.builder().id(id).name(name).active(true).build();
    }
}
