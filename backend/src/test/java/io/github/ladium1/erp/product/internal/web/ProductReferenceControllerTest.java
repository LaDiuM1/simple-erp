package io.github.ladium1.erp.product.internal.web;

import io.github.ladium1.erp.global.security.MenuPermissionEvaluator;
import io.github.ladium1.erp.global.web.PageResponse;
import io.github.ladium1.erp.product.internal.dto.ProductCategoryReferenceResponse;
import io.github.ladium1.erp.product.internal.dto.ProductReferenceResponse;
import io.github.ladium1.erp.product.internal.service.ProductCategoryService;
import io.github.ladium1.erp.product.internal.service.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.reset;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest({
        ProductController.class,
        ProductCategoryController.class,
        ProductReferenceController.class
})
@AutoConfigureMockMvc(addFilters = false)
@Import(ProductReferenceControllerTest.MethodSecurityTestConfig.class)
@WithMockUser
class ProductReferenceControllerTest {

    @TestConfiguration
    @EnableMethodSecurity
    static class MethodSecurityTestConfig {
    }

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProductService productService;

    @MockitoBean
    private ProductCategoryService productCategoryService;

    @MockitoBean(name = "menuPermissionEvaluator")
    private MenuPermissionEvaluator menuPermissionEvaluator;

    @BeforeEach
    void allow_contracts_only() {
        reset(menuPermissionEvaluator);
        given(menuPermissionEvaluator.canRead(any(), any())).willAnswer(invocation ->
                "CONTRACTS".equals(invocation.getArgument(1)));

        ProductReferenceResponse reference = ProductReferenceResponse.builder()
                .id(7L)
                .modelName("HLA-1530")
                .categoryName("평판 레이저")
                .supplierName("YAWEI")
                .active(true)
                .build();
        given(productService.searchReference(any(), any())).willReturn(
                new PageResponse<>(List.of(reference), 0, 20, 1, 1, false)
        );
        given(productService.getReference(7L)).willReturn(reference);
        given(productCategoryService.findReferences()).willReturn(List.of(
                ProductCategoryReferenceResponse.builder().id(3L).name("평판 레이저").build()
        ));
    }

    @ParameterizedTest
    @ValueSource(strings = {"CONTRACTS", "EQUIPMENTS"})
    @DisplayName("업무 권한은 최소 제품 참조만 조회하고 제품 관리 응답에는 접근하지 못한다")
    void work_reader_can_only_use_product_references(String menuCode) throws Exception {
        reset(menuPermissionEvaluator);
        given(menuPermissionEvaluator.canRead(any(), any())).willAnswer(invocation ->
                menuCode.equals(invocation.getArgument(1)));

        mockMvc.perform(get("/api/v1/products/reference"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].modelName").value("HLA-1530"))
                .andExpect(jsonPath("$.data.content[0].note").doesNotExist());

        mockMvc.perform(get("/api/v1/products/reference/{id}", 7L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.supplierName").value("YAWEI"))
                .andExpect(jsonPath("$.data.note").doesNotExist());

        mockMvc.perform(get("/api/v1/products/reference/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].name").value("평판 레이저"))
                .andExpect(jsonPath("$.data[0].productCount").doesNotExist());

        mockMvc.perform(get("/api/v1/products/summary"))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/v1/products/{id}", 7L))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/v1/products/categories"))
                .andExpect(status().isForbidden());
    }
}
