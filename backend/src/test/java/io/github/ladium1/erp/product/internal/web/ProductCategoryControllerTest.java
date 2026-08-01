package io.github.ladium1.erp.product.internal.web;

import io.github.ladium1.erp.global.exception.BusinessException;
import io.github.ladium1.erp.global.security.MenuPermissionEvaluator;
import io.github.ladium1.erp.product.internal.dto.ProductCategoryCreateRequest;
import io.github.ladium1.erp.product.internal.dto.ProductCategoryReorderRequest;
import io.github.ladium1.erp.product.internal.dto.ProductCategoryResponse;
import io.github.ladium1.erp.product.internal.dto.ProductCategoryUpdateRequest;
import io.github.ladium1.erp.product.internal.exception.ProductErrorCode;
import io.github.ladium1.erp.product.internal.service.ProductCategoryService;
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
import java.util.stream.LongStream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProductCategoryController.class)
@AutoConfigureMockMvc(addFilters = false)
class ProductCategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ProductCategoryService productCategoryService;

    @MockitoBean
    private MenuPermissionEvaluator menuPermissionEvaluator;

    @BeforeEach
    void allow_all_permissions() {
        given(menuPermissionEvaluator.canRead(any(), any())).willReturn(true);
        given(menuPermissionEvaluator.canWrite(any(), any())).willReturn(true);
    }

    @Test
    @DisplayName("카테고리 목록 조회 성공 — sortOrder 순 + 참조 모델 수")
    void find_all_success() throws Exception {
        // given
        ProductCategoryResponse flat = ProductCategoryResponse.builder()
                .id(1L).name("평판 레이저").sortOrder(1).productCount(3L).build();
        ProductCategoryResponse pipe = ProductCategoryResponse.builder()
                .id(2L).name("파이프 레이저").sortOrder(2).productCount(0L).build();
        given(productCategoryService.findAll()).willReturn(List.of(flat, pipe));

        // when & then
        mockMvc.perform(get("/api/v1/products/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].name").value("평판 레이저"))
                .andExpect(jsonPath("$.data[0].productCount").value(3))
                .andExpect(jsonPath("$.data[1].sortOrder").value(2));
    }

    @Test
    @DisplayName("카테고리 등록 성공")
    void create_success() throws Exception {
        // given
        ProductCategoryCreateRequest request = new ProductCategoryCreateRequest("로봇 용접기");
        given(productCategoryService.create(any())).willReturn(11L);

        // when & then
        mockMvc.perform(post("/api/v1/products/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(11));
    }

    @Test
    @DisplayName("중복 카테고리명 등록 시 409")
    void create_fail_duplicate_name() throws Exception {
        // given
        ProductCategoryCreateRequest request = new ProductCategoryCreateRequest("평판 레이저");
        willThrow(new BusinessException(ProductErrorCode.DUPLICATE_CATEGORY_NAME))
                .given(productCategoryService).create(any());

        // when & then
        mockMvc.perform(post("/api/v1/products/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("카테고리명 미입력 등록 시 400")
    void create_fail_blank_name() throws Exception {
        // given
        ProductCategoryCreateRequest request = new ProductCategoryCreateRequest("");

        // when & then
        mockMvc.perform(post("/api/v1/products/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("카테고리 수정 성공")
    void update_success() throws Exception {
        // given
        ProductCategoryUpdateRequest request = new ProductCategoryUpdateRequest("평판 레이저 (신형)");

        // when & then
        mockMvc.perform(put("/api/v1/products/categories/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNoContent());
        verify(productCategoryService).update(eq(1L), any());
    }

    @Test
    @DisplayName("카테고리 순서 일괄 재배치 성공")
    void reorder_success() throws Exception {
        // given
        ProductCategoryReorderRequest request = new ProductCategoryReorderRequest(List.of(2L, 1L, 3L));

        // when & then
        mockMvc.perform(put("/api/v1/products/categories/reorder")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNoContent());
        verify(productCategoryService).reorder(any());
    }

    @Test
    @DisplayName("순서 재배치 페이로드 불일치 시 400")
    void reorder_fail_invalid_payload() throws Exception {
        // given
        ProductCategoryReorderRequest request = new ProductCategoryReorderRequest(List.of(1L));
        willThrow(new BusinessException(ProductErrorCode.INVALID_REORDER_PAYLOAD))
                .given(productCategoryService).reorder(any());

        // when & then
        mockMvc.perform(put("/api/v1/products/categories/reorder")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("카테고리 재배치는 50개를 넘으면 서비스 호출 전에 400")
    void reorder_fail_request_limit_exceeded() throws Exception {
        ProductCategoryReorderRequest request = new ProductCategoryReorderRequest(
                LongStream.rangeClosed(0, 50).boxed().toList());

        mockMvc.perform(put("/api/v1/products/categories/reorder")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
        verify(productCategoryService, never()).reorder(any());
    }

    @Test
    @DisplayName("카테고리 삭제 성공")
    void delete_success() throws Exception {
        // when & then
        mockMvc.perform(delete("/api/v1/products/categories/{id}", 1L))
                .andExpect(status().isNoContent());
        verify(productCategoryService).delete(1L);
    }

    @Test
    @DisplayName("제품 모델이 참조 중인 카테고리 삭제 시 400")
    void delete_fail_in_use() throws Exception {
        // given
        willThrow(new BusinessException(ProductErrorCode.CATEGORY_IN_USE))
                .given(productCategoryService).delete(1L);

        // when & then
        mockMvc.perform(delete("/api/v1/products/categories/{id}", 1L))
                .andExpect(status().isBadRequest());
    }
}
