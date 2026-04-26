package io.github.ladium1.erp.salescontact.internal.web;

import io.github.ladium1.erp.global.exception.BusinessException;
import io.github.ladium1.erp.global.security.MenuPermissionEvaluator;
import io.github.ladium1.erp.global.web.PageResponse;
import io.github.ladium1.erp.salescontact.internal.dto.SalesContactCreateRequest;
import io.github.ladium1.erp.salescontact.internal.dto.SalesContactDetailResponse;
import io.github.ladium1.erp.salescontact.internal.dto.SalesContactEmploymentCreateRequest;
import io.github.ladium1.erp.salescontact.internal.dto.SalesContactEmploymentResponse;
import io.github.ladium1.erp.salescontact.internal.dto.SalesContactEmploymentTerminateRequest;
import io.github.ladium1.erp.salescontact.internal.dto.SalesContactSummaryResponse;
import io.github.ladium1.erp.salescontact.internal.entity.DepartureType;
import io.github.ladium1.erp.salescontact.internal.exception.SalesContactErrorCode;
import io.github.ladium1.erp.salescontact.internal.service.SalesContactService;
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

import java.time.LocalDate;
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

@WebMvcTest(SalesContactController.class)
@AutoConfigureMockMvc(addFilters = false)
class SalesContactControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private SalesContactService salesContactService;

    @MockitoBean
    private MenuPermissionEvaluator menuPermissionEvaluator;

    @BeforeEach
    void allow_all_permissions() {
        given(menuPermissionEvaluator.canRead(any(), any())).willReturn(true);
        given(menuPermissionEvaluator.canWrite(any(), any())).willReturn(true);
    }

    @Test
    @DisplayName("명부 검색 성공")
    void search_success() throws Exception {
        SalesContactSummaryResponse summary = SalesContactSummaryResponse.builder()
                .id(1L).name("정대성").mobilePhone("010-1111-2222").email("ds@daesung.co.kr")
                .currentCompanyName("대성상사").currentPosition("팀장")
                .build();
        PageResponse<SalesContactSummaryResponse> page = new PageResponse<>(
                List.of(summary), 0, 20, 1, 1, false
        );
        given(salesContactService.search(any(), any())).willReturn(page);

        mockMvc.perform(get("/api/v1/sales-contacts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].name").value("정대성"))
                .andExpect(jsonPath("$.data.content[0].currentCompanyName").value("대성상사"));
    }

    @Test
    @DisplayName("명부 상세 조회 성공")
    void get_detail_success() throws Exception {
        SalesContactDetailResponse detail = SalesContactDetailResponse.builder()
                .id(7L).name("정대성").employments(List.of()).build();
        given(salesContactService.getDetail(7L)).willReturn(detail);

        mockMvc.perform(get("/api/v1/sales-contacts/{id}", 7L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("정대성"));
    }

    @Test
    @DisplayName("존재하지 않는 명부 조회 시 404")
    void get_detail_fail_not_found() throws Exception {
        given(salesContactService.getDetail(99L))
                .willThrow(new BusinessException(SalesContactErrorCode.CONTACT_NOT_FOUND));

        mockMvc.perform(get("/api/v1/sales-contacts/{id}", 99L))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("고객사별 재직 명부 조회 성공")
    void find_employments_by_customer_success() throws Exception {
        SalesContactEmploymentResponse emp = SalesContactEmploymentResponse.builder()
                .id(1L).contactId(7L).contactName("정대성").customerId(10L).customerName("대성상사")
                .position("팀장").startDate(LocalDate.of(2026, 1, 1)).active(true).build();
        given(salesContactService.findEmploymentsByCustomerId(10L)).willReturn(List.of(emp));

        mockMvc.perform(get("/api/v1/sales-contacts/employments").param("customerId", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].contactName").value("정대성"))
                .andExpect(jsonPath("$.data[0].active").value(true));
    }

    @Test
    @DisplayName("명부 등록 성공")
    void create_success() throws Exception {
        SalesContactCreateRequest request = new SalesContactCreateRequest(
                "정대성", null, "010-1111-2222", null,
                "ds@daesung.co.kr", null, null, null, null
        );
        given(salesContactService.create(any())).willReturn(42L);

        mockMvc.perform(post("/api/v1/sales-contacts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(42));
    }

    @Test
    @DisplayName("재직 등록 성공")
    void create_employment_success() throws Exception {
        SalesContactEmploymentCreateRequest request = new SalesContactEmploymentCreateRequest(
                10L, null, "팀장", "영업1팀", LocalDate.of(2026, 4, 1)
        );
        given(salesContactService.createEmployment(eq(7L), any())).willReturn(50L);

        mockMvc.perform(post("/api/v1/sales-contacts/{contactId}/employments", 7L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(50));
    }

    @Test
    @DisplayName("재직 종료 성공")
    void terminate_employment_success() throws Exception {
        SalesContactEmploymentTerminateRequest request = new SalesContactEmploymentTerminateRequest(
                LocalDate.of(2026, 4, 30), DepartureType.JOB_CHANGE, "이직"
        );

        mockMvc.perform(put("/api/v1/sales-contacts/employments/{id}/terminate", 7L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNoContent());
        verify(salesContactService).terminateEmployment(eq(7L), any());
    }

    @Test
    @DisplayName("재직 종료 실패 — 이미 종료된 재직 (400)")
    void terminate_employment_fail_already_terminated() throws Exception {
        SalesContactEmploymentTerminateRequest request = new SalesContactEmploymentTerminateRequest(
                LocalDate.of(2026, 4, 30), DepartureType.OTHER, null
        );
        willThrow(new BusinessException(SalesContactErrorCode.EMPLOYMENT_ALREADY_TERMINATED))
                .given(salesContactService).terminateEmployment(eq(7L), any());

        mockMvc.perform(put("/api/v1/sales-contacts/employments/{id}/terminate", 7L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("재직 삭제 성공")
    void delete_employment_success() throws Exception {
        mockMvc.perform(delete("/api/v1/sales-contacts/employments/{id}", 7L))
                .andExpect(status().isNoContent());
        verify(salesContactService).deleteEmployment(7L);
    }

    @Test
    @DisplayName("명부 삭제 성공")
    void delete_contact_success() throws Exception {
        mockMvc.perform(delete("/api/v1/sales-contacts/{id}", 7L))
                .andExpect(status().isNoContent());
        verify(salesContactService).delete(7L);
    }
}
