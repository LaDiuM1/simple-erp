package io.github.ladium1.erp.equipment.internal.web;

import io.github.ladium1.erp.equipment.internal.dto.EquipmentCreateRequest;
import io.github.ladium1.erp.equipment.internal.dto.EquipmentDetailResponse;
import io.github.ladium1.erp.equipment.internal.dto.EquipmentSearchCondition;
import io.github.ladium1.erp.equipment.internal.dto.EquipmentSummaryResponse;
import io.github.ladium1.erp.equipment.internal.dto.EquipmentUpdateRequest;
import io.github.ladium1.erp.equipment.internal.dto.WarrantyFilter;
import io.github.ladium1.erp.equipment.internal.entity.OutputUnit;
import io.github.ladium1.erp.equipment.internal.exception.EquipmentErrorCode;
import io.github.ladium1.erp.equipment.internal.service.EquipmentService;
import io.github.ladium1.erp.global.exception.BusinessException;
import io.github.ladium1.erp.global.security.MenuPermissionEvaluator;
import io.github.ladium1.erp.global.web.PageResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
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

@WebMvcTest(EquipmentController.class)
@AutoConfigureMockMvc(addFilters = false)
class EquipmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private EquipmentService equipmentService;

    @MockitoBean
    private MenuPermissionEvaluator menuPermissionEvaluator;

    @BeforeEach
    void allow_all_permissions() {
        given(menuPermissionEvaluator.canRead(any(), any())).willReturn(true);
        given(menuPermissionEvaluator.canWrite(any(), any())).willReturn(true);
    }

    @Test
    @DisplayName("설비 대장 목록 검색 성공")
    void search_success() throws Exception {
        // given
        EquipmentSummaryResponse summary = EquipmentSummaryResponse.builder()
                .id(1L).customerName("대성상사").supplierName("YAWEI")
                .productModelName("HLA-1530").categoryName("평판 레이저")
                .contractNo("CT2026-001")
                .generalWarrantyEndDate(LocalDate.of(2027, 3, 2))
                .warrantyInsurance(true)
                .build();
        PageResponse<EquipmentSummaryResponse> page = new PageResponse<>(
                List.of(summary), 0, 20, 1, 1, false
        );
        given(equipmentService.search(any(), any())).willReturn(page);

        // when & then
        mockMvc.perform(get("/api/v1/equipments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].customerName").value("대성상사"))
                .andExpect(jsonPath("$.data.content[0].contractNo").value("CT2026-001"))
                .andExpect(jsonPath("$.data.totalElements").value(1));
    }

    @Test
    @DisplayName("검색 조건 바인딩 — 보증 상태 필터가 condition 으로 전달")
    void search_condition_binding() throws Exception {
        // given
        given(equipmentService.search(any(), any()))
                .willReturn(new PageResponse<>(List.of(), 0, 20, 0, 0, false));

        // when
        mockMvc.perform(get("/api/v1/equipments").param("warranty", "EXPIRING"))
                .andExpect(status().isOk());

        // then
        ArgumentCaptor<EquipmentSearchCondition> captor = ArgumentCaptor.forClass(EquipmentSearchCondition.class);
        verify(equipmentService).search(captor.capture(), any());
        assertThat(captor.getValue().warranty()).isEqualTo(WarrantyFilter.EXPIRING);
    }

    @Test
    @DisplayName("설비 상세 조회 성공")
    void get_detail_success() throws Exception {
        // given
        EquipmentDetailResponse detail = EquipmentDetailResponse.builder()
                .id(1L).customerName("대성상사").productModelName("HLA-1530")
                .oscillatorWarrantyEndDate(LocalDate.of(2029, 3, 2))
                .warrantyInsurance(false)
                .build();
        given(equipmentService.getDetail(1L)).willReturn(detail);

        // when & then
        mockMvc.perform(get("/api/v1/equipments/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.customerName").value("대성상사"))
                .andExpect(jsonPath("$.data.oscillatorWarrantyEndDate").value("2029-03-02"));
    }

    @Test
    @DisplayName("존재하지 않는 설비 조회 시 404")
    void get_detail_fail_not_found() throws Exception {
        // given
        given(equipmentService.getDetail(99L))
                .willThrow(new BusinessException(EquipmentErrorCode.EQUIPMENT_NOT_FOUND));

        // when & then
        mockMvc.perform(get("/api/v1/equipments/{id}", 99L))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("설비 수동 등록 성공")
    void create_success() throws Exception {
        // given
        EquipmentCreateRequest request = new EquipmentCreateRequest(
                1L, 3L, new BigDecimal("12"), OutputUnit.KW,
                "SN-001", "김포시 설치공장", LocalDate.of(2026, 3, 2), null,
                LocalDate.of(2026, 3, 2), 36, 12, false, null
        );
        given(equipmentService.create(any())).willReturn(42L);

        // when & then
        mockMvc.perform(post("/api/v1/equipments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(42));
    }

    @Test
    @DisplayName("고객사 미입력 등록 시 400")
    void create_fail_missing_customer() throws Exception {
        // given
        EquipmentCreateRequest request = new EquipmentCreateRequest(
                null, 3L, null, null, null, null, null, null, null, null, null, false, null
        );

        // when & then
        mockMvc.perform(post("/api/v1/equipments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("설비 수정 성공")
    void update_success() throws Exception {
        // given
        EquipmentUpdateRequest request = new EquipmentUpdateRequest(
                1L, 3L, new BigDecimal("12"), OutputUnit.KW,
                "SN-002", null, null, null, LocalDate.of(2026, 4, 1), 24, 12, true, null
        );

        // when & then
        mockMvc.perform(put("/api/v1/equipments/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNoContent());
        verify(equipmentService).update(eq(1L), any());
    }

    @Test
    @DisplayName("설비 삭제 성공")
    void delete_success() throws Exception {
        // when & then
        mockMvc.perform(delete("/api/v1/equipments/{id}", 1L))
                .andExpect(status().isNoContent());
        verify(equipmentService).delete(1L);
    }

    @Test
    @DisplayName("설비 일괄 삭제 성공")
    void delete_all_success() throws Exception {
        // when & then
        mockMvc.perform(delete("/api/v1/equipments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(List.of(1L, 2L))))
                .andExpect(status().isNoContent());
        verify(equipmentService).deleteAll(List.of(1L, 2L));
    }
}
