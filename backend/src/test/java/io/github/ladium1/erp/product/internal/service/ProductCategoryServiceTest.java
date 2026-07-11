package io.github.ladium1.erp.product.internal.service;

import io.github.ladium1.erp.global.exception.BusinessException;
import io.github.ladium1.erp.product.internal.dto.ProductCategoryCreateRequest;
import io.github.ladium1.erp.product.internal.dto.ProductCategoryReorderRequest;
import io.github.ladium1.erp.product.internal.dto.ProductCategoryResponse;
import io.github.ladium1.erp.product.internal.dto.ProductCategoryUpdateRequest;
import io.github.ladium1.erp.product.internal.entity.ProductCategory;
import io.github.ladium1.erp.product.internal.exception.ProductErrorCode;
import io.github.ladium1.erp.product.internal.repository.ProductCategoryRepository;
import io.github.ladium1.erp.product.internal.repository.ProductRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ProductCategoryServiceTest {

    @InjectMocks
    private ProductCategoryService productCategoryService;

    @Mock private ProductCategoryRepository productCategoryRepository;
    @Mock private ProductRepository productRepository;

    @Test
    @DisplayName("findAll — sortOrder 순 + 참조 모델 수 포함")
    void find_all_success() {
        // given
        ProductCategory flat = mockCategory(1L, "평판 레이저", 1);
        ProductCategory pipe = mockCategory(2L, "파이프 레이저", 2);
        given(productCategoryRepository.findAllByOrderBySortOrderAsc()).willReturn(List.of(flat, pipe));
        given(productRepository.countGroupByCategory()).willReturn(Map.of(1L, 3L));

        // when
        List<ProductCategoryResponse> result = productCategoryService.findAll();

        // then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).name()).isEqualTo("평판 레이저");
        assertThat(result.get(0).productCount()).isEqualTo(3L);
        assertThat(result.get(1).productCount()).isZero();
    }

    @Test
    @DisplayName("create 성공 — max+1 sortOrder 자동 부여")
    void create_success() {
        // given
        ProductCategoryCreateRequest request = new ProductCategoryCreateRequest(" 로봇 용접기 ");
        given(productCategoryRepository.existsByName("로봇 용접기")).willReturn(false);
        given(productCategoryRepository.findTopByOrderBySortOrderDesc())
                .willReturn(Optional.of(mockCategory(10L, "기타", 10)));
        ProductCategory saved = mockCategory(11L, "로봇 용접기", 11);
        given(productCategoryRepository.save(any(ProductCategory.class))).willReturn(saved);

        // when
        Long id = productCategoryService.create(request);

        // then
        assertThat(id).isEqualTo(11L);
    }

    @Test
    @DisplayName("create — 등록된 카테고리가 없으면 sortOrder 1 부터 시작")
    void create_first_category_starts_from_order_1() {
        // given
        ProductCategoryCreateRequest request = new ProductCategoryCreateRequest("평판 레이저");
        given(productCategoryRepository.existsByName("평판 레이저")).willReturn(false);
        given(productCategoryRepository.findTopByOrderBySortOrderDesc()).willReturn(Optional.empty());
        ProductCategory saved = mockCategory(1L, "평판 레이저", 1);
        given(productCategoryRepository.save(any(ProductCategory.class))).willReturn(saved);

        // when
        Long id = productCategoryService.create(request);

        // then
        assertThat(id).isEqualTo(1L);
        verify(productCategoryRepository).save(any(ProductCategory.class));
    }

    @Test
    @DisplayName("create 실패 — 중복 카테고리명 시 DUPLICATE_CATEGORY_NAME")
    void create_fail_duplicate_name() {
        // given
        ProductCategoryCreateRequest request = new ProductCategoryCreateRequest("평판 레이저");
        given(productCategoryRepository.existsByName("평판 레이저")).willReturn(true);

        // when & then
        assertThatThrownBy(() -> productCategoryService.create(request))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ProductErrorCode.DUPLICATE_CATEGORY_NAME);
        verify(productCategoryRepository, never()).save(any());
    }

    @Test
    @DisplayName("update 성공 — 이름 변경")
    void update_success() {
        // given
        ProductCategory category = mockCategory(1L, "평판 레이저", 1);
        given(productCategoryRepository.findById(1L)).willReturn(Optional.of(category));
        given(productCategoryRepository.existsByNameAndIdNot("평판 레이저 (신형)", 1L)).willReturn(false);

        // when
        productCategoryService.update(1L, new ProductCategoryUpdateRequest("평판 레이저 (신형)"));

        // then
        assertThat(category.getName()).isEqualTo("평판 레이저 (신형)");
    }

    @Test
    @DisplayName("update 실패 — 다른 카테고리가 쓰는 이름으로 변경 시 DUPLICATE_CATEGORY_NAME")
    void update_fail_duplicate_name() {
        // given
        ProductCategory category = mockCategory(1L, "평판 레이저", 1);
        given(productCategoryRepository.findById(1L)).willReturn(Optional.of(category));
        given(productCategoryRepository.existsByNameAndIdNot("파이프 레이저", 1L)).willReturn(true);

        // when & then
        assertThatThrownBy(() -> productCategoryService.update(1L, new ProductCategoryUpdateRequest("파이프 레이저")))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ProductErrorCode.DUPLICATE_CATEGORY_NAME);
    }

    @Test
    @DisplayName("update 실패 — 존재하지 않는 카테고리")
    void update_fail_not_found() {
        // given
        given(productCategoryRepository.findById(99L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> productCategoryService.update(99L, new ProductCategoryUpdateRequest("이름")))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ProductErrorCode.CATEGORY_NOT_FOUND);
    }

    @Test
    @DisplayName("delete 성공")
    void delete_success() {
        // given
        given(productCategoryRepository.existsById(1L)).willReturn(true);
        given(productRepository.existsByCategoryId(1L)).willReturn(false);

        // when
        productCategoryService.delete(1L);

        // then
        verify(productCategoryRepository).deleteById(1L);
    }

    @Test
    @DisplayName("delete 실패 — 제품 모델이 참조 중이면 CATEGORY_IN_USE")
    void delete_fail_in_use() {
        // given
        given(productCategoryRepository.existsById(1L)).willReturn(true);
        given(productRepository.existsByCategoryId(1L)).willReturn(true);

        // when & then
        assertThatThrownBy(() -> productCategoryService.delete(1L))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ProductErrorCode.CATEGORY_IN_USE);
        verify(productCategoryRepository, never()).deleteById(any());
    }

    @Test
    @DisplayName("delete 실패 — 존재하지 않는 카테고리")
    void delete_fail_not_found() {
        // given
        given(productCategoryRepository.existsById(99L)).willReturn(false);

        // when & then
        assertThatThrownBy(() -> productCategoryService.delete(99L))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ProductErrorCode.CATEGORY_NOT_FOUND);
        verify(productCategoryRepository, never()).deleteById(any());
    }

    @Test
    @DisplayName("reorder 성공 — 요청 순서대로 1, 2, 3 재부여")
    void reorder_success() {
        // given
        ProductCategory c1 = mockCategory(1L, "평판 레이저", 1);
        ProductCategory c2 = mockCategory(2L, "파이프 레이저", 2);
        ProductCategory c3 = mockCategory(3L, "절곡기", 3);
        given(productCategoryRepository.findAll()).willReturn(List.of(c1, c2, c3));

        // 파이프 → 평판 → 절곡기 순으로 재배치
        ProductCategoryReorderRequest request = new ProductCategoryReorderRequest(List.of(2L, 1L, 3L));

        // when
        productCategoryService.reorder(request);

        // then
        assertThat(c2.getSortOrder()).isEqualTo(1);
        assertThat(c1.getSortOrder()).isEqualTo(2);
        assertThat(c3.getSortOrder()).isEqualTo(3);
    }

    @Test
    @DisplayName("reorder 실패 — 중복된 ID 포함")
    void reorder_fail_duplicate_ids() {
        ProductCategoryReorderRequest request = new ProductCategoryReorderRequest(List.of(1L, 2L, 1L));

        assertThatThrownBy(() -> productCategoryService.reorder(request))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ProductErrorCode.INVALID_REORDER_PAYLOAD);
    }

    @Test
    @DisplayName("reorder 실패 — DB 의 카테고리 수와 요청 ID 수 불일치")
    void reorder_fail_size_mismatch() {
        // given — DB 에는 2건, 요청에는 1건
        given(productCategoryRepository.findAll()).willReturn(List.of(
                mockCategory(1L, "평판 레이저", 1),
                mockCategory(2L, "파이프 레이저", 2)
        ));
        ProductCategoryReorderRequest request = new ProductCategoryReorderRequest(List.of(1L));

        // when & then
        assertThatThrownBy(() -> productCategoryService.reorder(request))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ProductErrorCode.INVALID_REORDER_PAYLOAD);
    }

    @Test
    @DisplayName("reorder 실패 — DB 에 없는 ID 가 섞여 있음")
    void reorder_fail_unknown_id() {
        // given — DB 에는 1, 2 인데 요청에는 1, 999
        given(productCategoryRepository.findAll()).willReturn(List.of(
                mockCategory(1L, "평판 레이저", 1),
                mockCategory(2L, "파이프 레이저", 2)
        ));
        ProductCategoryReorderRequest request = new ProductCategoryReorderRequest(List.of(1L, 999L));

        // when & then
        assertThatThrownBy(() -> productCategoryService.reorder(request))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ProductErrorCode.INVALID_REORDER_PAYLOAD);
    }

    private ProductCategory mockCategory(Long id, String name, int sortOrder) {
        ProductCategory category = ProductCategory.builder()
                .name(name)
                .sortOrder(sortOrder)
                .build();
        ReflectionTestUtils.setField(category, "id", id);
        return category;
    }
}
