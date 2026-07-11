package io.github.ladium1.erp.product.internal.web;

import io.github.ladium1.erp.global.exception.BusinessException;
import io.github.ladium1.erp.global.security.MenuPermissionEvaluator;
import io.github.ladium1.erp.global.web.PageResponse;
import io.github.ladium1.erp.product.internal.dto.ProductCreateRequest;
import io.github.ladium1.erp.product.internal.dto.ProductDetailResponse;
import io.github.ladium1.erp.product.internal.dto.ProductSummaryResponse;
import io.github.ladium1.erp.product.internal.dto.ProductUpdateRequest;
import io.github.ladium1.erp.product.internal.exception.ProductErrorCode;
import io.github.ladium1.erp.product.internal.service.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProductController.class)
@AutoConfigureMockMvc(addFilters = false)
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ProductService productService;

    @MockitoBean
    private MenuPermissionEvaluator menuPermissionEvaluator;

    @BeforeEach
    void allow_all_permissions() {
        given(menuPermissionEvaluator.canRead(any(), any())).willReturn(true);
        given(menuPermissionEvaluator.canWrite(any(), any())).willReturn(true);
    }

    @Test
    @DisplayName("제품 모델 목록 검색 성공")
    void search_success() throws Exception {
        // given
        ProductSummaryResponse summary = ProductSummaryResponse.builder()
                .id(1L).categoryId(1L).categoryName("평판 레이저").modelName("HLA-1530")
                .supplierId(1L).supplierName("YAWEI").active(true).build();
        PageResponse<ProductSummaryResponse> page = new PageResponse<>(
                List.of(summary), 0, 20, 1, 1, false
        );
        given(productService.search(any(), any())).willReturn(page);

        // when & then
        mockMvc.perform(get("/api/v1/products/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].modelName").value("HLA-1530"))
                .andExpect(jsonPath("$.data.content[0].categoryName").value("평판 레이저"))
                .andExpect(jsonPath("$.data.content[0].supplierName").value("YAWEI"))
                .andExpect(jsonPath("$.data.totalElements").value(1));
    }

    @Test
    @DisplayName("제품 모델 상세 조회 성공")
    void get_detail_success() throws Exception {
        // given
        ProductDetailResponse detail = ProductDetailResponse.builder()
                .id(7L).categoryId(3L).categoryName("파이프 레이저").modelName("DAP-3S-360")
                .supplierId(2L).supplierName("ACME").note("비고").active(true).build();
        given(productService.getDetail(7L)).willReturn(detail);

        // when & then
        mockMvc.perform(get("/api/v1/products/{id}", 7L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(7))
                .andExpect(jsonPath("$.data.modelName").value("DAP-3S-360"))
                .andExpect(jsonPath("$.data.categoryName").value("파이프 레이저"));
    }

    @Test
    @DisplayName("존재하지 않는 제품 모델 조회 시 404")
    void get_detail_fail_not_found() throws Exception {
        // given
        given(productService.getDetail(99L))
                .willThrow(new BusinessException(ProductErrorCode.PRODUCT_NOT_FOUND));

        // when & then
        mockMvc.perform(get("/api/v1/products/{id}", 99L))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("제품 모델 등록 성공")
    void create_success() throws Exception {
        // given
        ProductCreateRequest request = new ProductCreateRequest(1L, "HLA-1530", 1L, null, true);
        given(productService.create(any())).willReturn(42L);

        // when & then
        mockMvc.perform(post("/api/v1/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(42));
    }

    @Test
    @DisplayName("중복 모델명으로 등록 시 409")
    void create_fail_duplicate_model_name() throws Exception {
        // given
        ProductCreateRequest request = new ProductCreateRequest(1L, "HLA-1530", 1L, null, true);
        willThrow(new BusinessException(ProductErrorCode.DUPLICATE_MODEL_NAME))
                .given(productService).create(any());

        // when & then
        mockMvc.perform(post("/api/v1/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("카테고리 미입력 등록 시 400")
    void create_fail_missing_category() throws Exception {
        // given
        ProductCreateRequest request = new ProductCreateRequest(null, "HLA-1530", 1L, null, true);

        // when & then
        mockMvc.perform(post("/api/v1/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("제품 모델 수정 성공")
    void update_success() throws Exception {
        // given
        ProductUpdateRequest request = new ProductUpdateRequest(3L, "DAP-3S-360", 2L, null, true);

        // when & then
        mockMvc.perform(put("/api/v1/products/{id}", 7L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNoContent());
        verify(productService).update(eq(7L), any());
    }

    @Test
    @DisplayName("제품 모델 삭제 성공")
    void delete_success() throws Exception {
        // when & then
        mockMvc.perform(delete("/api/v1/products/{id}", 7L))
                .andExpect(status().isNoContent());
        verify(productService).delete(7L);
    }

    @Test
    @DisplayName("제품 모델 일괄 삭제 성공")
    void delete_all_success() throws Exception {
        // when & then
        mockMvc.perform(delete("/api/v1/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(List.of(1L, 2L))))
                .andExpect(status().isNoContent());
        verify(productService).deleteAll(List.of(1L, 2L));
    }
}
