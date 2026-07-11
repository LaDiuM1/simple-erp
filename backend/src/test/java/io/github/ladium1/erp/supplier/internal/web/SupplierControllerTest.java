package io.github.ladium1.erp.supplier.internal.web;

import io.github.ladium1.erp.global.exception.BusinessException;
import io.github.ladium1.erp.global.security.MenuPermissionEvaluator;
import io.github.ladium1.erp.global.web.PageResponse;
import io.github.ladium1.erp.product.internal.exception.ProductErrorCode;
import io.github.ladium1.erp.supplier.api.dto.SupplierInfo;
import io.github.ladium1.erp.supplier.internal.dto.SupplierCreateRequest;
import io.github.ladium1.erp.supplier.internal.dto.SupplierDetailResponse;
import io.github.ladium1.erp.supplier.internal.dto.SupplierSummaryResponse;
import io.github.ladium1.erp.supplier.internal.dto.SupplierUpdateRequest;
import io.github.ladium1.erp.supplier.internal.exception.SupplierErrorCode;
import io.github.ladium1.erp.supplier.internal.service.SupplierService;
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

@WebMvcTest(SupplierController.class)
@AutoConfigureMockMvc(addFilters = false)
class SupplierControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private SupplierService supplierService;

    @MockitoBean
    private MenuPermissionEvaluator menuPermissionEvaluator;

    @BeforeEach
    void allow_all_permissions() {
        given(menuPermissionEvaluator.canRead(any(), any())).willReturn(true);
        given(menuPermissionEvaluator.canWrite(any(), any())).willReturn(true);
    }

    @Test
    @DisplayName("공급사 reference 목록 조회 성공")
    void find_all_success() throws Exception {
        // given
        SupplierInfo info = SupplierInfo.builder().id(1L).name("YAWEI").nameKo("야웨이").active(true).build();
        given(supplierService.findAll()).willReturn(List.of(info));

        // when & then
        mockMvc.perform(get("/api/v1/suppliers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].name").value("YAWEI"))
                .andExpect(jsonPath("$.data[0].nameKo").value("야웨이"));
    }

    @Test
    @DisplayName("공급사 목록 검색 성공")
    void search_success() throws Exception {
        // given
        SupplierSummaryResponse summary = SupplierSummaryResponse.builder()
                .id(1L).name("YAWEI").nameKo("야웨이").country("중국").active(true).build();
        PageResponse<SupplierSummaryResponse> page = new PageResponse<>(
                List.of(summary), 0, 20, 1, 1, false
        );
        given(supplierService.search(any(), any())).willReturn(page);

        // when & then
        mockMvc.perform(get("/api/v1/suppliers/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].name").value("YAWEI"))
                .andExpect(jsonPath("$.data.totalElements").value(1));
    }

    @Test
    @DisplayName("공급사 상세 조회 성공")
    void get_detail_success() throws Exception {
        // given
        SupplierDetailResponse detail = SupplierDetailResponse.builder()
                .id(7L).name("BAISHENG").nameKo("바이셩").country("중국").note("비고").active(true).build();
        given(supplierService.getDetail(7L)).willReturn(detail);

        // when & then
        mockMvc.perform(get("/api/v1/suppliers/{id}", 7L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(7))
                .andExpect(jsonPath("$.data.name").value("BAISHENG"));
    }

    @Test
    @DisplayName("존재하지 않는 공급사 조회 시 404")
    void get_detail_fail_not_found() throws Exception {
        // given
        given(supplierService.getDetail(99L))
                .willThrow(new BusinessException(SupplierErrorCode.SUPPLIER_NOT_FOUND));

        // when & then
        mockMvc.perform(get("/api/v1/suppliers/{id}", 99L))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("공급사 등록 성공")
    void create_success() throws Exception {
        // given
        SupplierCreateRequest request = new SupplierCreateRequest("ACME", null, "중국", null, true);
        given(supplierService.create(any())).willReturn(42L);

        // when & then
        mockMvc.perform(post("/api/v1/suppliers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(42));
    }

    @Test
    @DisplayName("중복 공급사명으로 등록 시 409")
    void create_fail_duplicate_name() throws Exception {
        // given
        SupplierCreateRequest request = new SupplierCreateRequest("YAWEI", null, null, null, true);
        willThrow(new BusinessException(SupplierErrorCode.DUPLICATE_NAME))
                .given(supplierService).create(any());

        // when & then
        mockMvc.perform(post("/api/v1/suppliers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("공급사명 미입력 등록 시 400")
    void create_fail_blank_name() throws Exception {
        // given
        SupplierCreateRequest request = new SupplierCreateRequest("", null, null, null, true);

        // when & then
        mockMvc.perform(post("/api/v1/suppliers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("공급사 수정 성공")
    void update_success() throws Exception {
        // given
        SupplierUpdateRequest request = new SupplierUpdateRequest("YAWEI", "야웨이", "중국", null, false);

        // when & then
        mockMvc.perform(put("/api/v1/suppliers/{id}", 7L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNoContent());
        verify(supplierService).update(eq(7L), any());
    }

    @Test
    @DisplayName("공급사 삭제 성공")
    void delete_success() throws Exception {
        // when & then
        mockMvc.perform(delete("/api/v1/suppliers/{id}", 7L))
                .andExpect(status().isNoContent());
        verify(supplierService).delete(7L);
    }

    @Test
    @DisplayName("제품 모델이 참조 중인 공급사 삭제 시 400")
    void delete_fail_in_use() throws Exception {
        // given
        willThrow(new BusinessException(ProductErrorCode.SUPPLIER_IN_USE))
                .given(supplierService).delete(7L);

        // when & then
        mockMvc.perform(delete("/api/v1/suppliers/{id}", 7L))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("공급사 일괄 삭제 성공")
    void delete_all_success() throws Exception {
        // when & then
        mockMvc.perform(delete("/api/v1/suppliers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(List.of(1L, 2L))))
                .andExpect(status().isNoContent());
        verify(supplierService).deleteAll(List.of(1L, 2L));
    }
}
