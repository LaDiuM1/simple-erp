package io.github.ladium1.erp.customer.internal.web;

import io.github.ladium1.erp.customer.internal.dto.CustomerCreateRequest;
import io.github.ladium1.erp.customer.internal.dto.CustomerDetailResponse;
import io.github.ladium1.erp.customer.internal.dto.CustomerSummaryResponse;
import io.github.ladium1.erp.customer.internal.dto.CustomerUpdateRequest;
import io.github.ladium1.erp.customer.internal.entity.CustomerStatus;
import io.github.ladium1.erp.customer.internal.entity.CustomerType;
import io.github.ladium1.erp.customer.internal.exception.CustomerErrorCode;
import io.github.ladium1.erp.customer.internal.service.CustomerService;
import io.github.ladium1.erp.global.exception.BusinessException;
import io.github.ladium1.erp.global.security.MenuPermissionEvaluator;
import io.github.ladium1.erp.global.web.PageResponse;
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

@WebMvcTest(CustomerController.class)
@AutoConfigureMockMvc(addFilters = false)
class CustomerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CustomerService customerService;

    @MockitoBean
    private MenuPermissionEvaluator menuPermissionEvaluator;

    @BeforeEach
    void allow_all_permissions() {
        given(menuPermissionEvaluator.canRead(any(), any())).willReturn(true);
        given(menuPermissionEvaluator.canWrite(any(), any())).willReturn(true);
    }

    @Test
    @DisplayName("고객사 목록 검색 성공")
    void search_success() throws Exception {
        // given
        CustomerSummaryResponse summary = CustomerSummaryResponse.builder()
                .id(1L).code("C0001").name("대성상사")
                .type(CustomerType.GENERAL).status(CustomerStatus.ACTIVE).build();
        PageResponse<CustomerSummaryResponse> page = new PageResponse<>(
                List.of(summary), 0, 20, 1, 1, false
        );
        given(customerService.search(any(), any())).willReturn(page);

        // when & then
        mockMvc.perform(get("/api/v1/customers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].code").value("C0001"))
                .andExpect(jsonPath("$.data.totalElements").value(1));
    }

    @Test
    @DisplayName("고객사 상세 조회 성공")
    void get_detail_success() throws Exception {
        // given
        CustomerDetailResponse detail = CustomerDetailResponse.builder()
                .id(7L).code("C0007").name("팀장상사")
                .type(CustomerType.KEY_ACCOUNT).status(CustomerStatus.ACTIVE).build();
        given(customerService.getDetail(7L)).willReturn(detail);

        // when & then
        mockMvc.perform(get("/api/v1/customers/{id}", 7L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(7))
                .andExpect(jsonPath("$.data.name").value("팀장상사"));
    }

    @Test
    @DisplayName("존재하지 않는 고객사 조회 시 404")
    void get_detail_fail_not_found() throws Exception {
        given(customerService.getDetail(99L))
                .willThrow(new BusinessException(CustomerErrorCode.CUSTOMER_NOT_FOUND));

        mockMvc.perform(get("/api/v1/customers/{id}", 99L))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("코드 사용 가능 여부 조회 성공")
    void check_code_availability_success() throws Exception {
        given(customerService.isCodeAvailable("C9999")).willReturn(true);

        mockMvc.perform(get("/api/v1/customers/code-availability").param("code", "C9999"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.available").value(true));
    }

    @Test
    @DisplayName("사업자번호 사용 가능 여부 조회 성공")
    void check_biz_reg_no_availability_success() throws Exception {
        given(customerService.isBizRegNoAvailable("999-99-99999")).willReturn(true);

        mockMvc.perform(get("/api/v1/customers/bizregno-availability").param("bizRegNo", "999-99-99999"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.available").value(true));
    }

    @Test
    @DisplayName("고객사 등록 성공")
    void create_success() throws Exception {
        // given
        CustomerCreateRequest request = baseCreateRequest("신규고객사");
        given(customerService.create(any())).willReturn(42L);

        // when & then
        mockMvc.perform(post("/api/v1/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(42));
    }

    @Test
    @DisplayName("중복 코드 등록 시 409")
    void create_fail_duplicate_code() throws Exception {
        // given
        CustomerCreateRequest request = baseCreateRequest("중복");
        willThrow(new BusinessException(CustomerErrorCode.DUPLICATE_CODE))
                .given(customerService).create(any());

        // when & then
        mockMvc.perform(post("/api/v1/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("중복 사업자번호 등록 시 409")
    void create_fail_duplicate_biz_reg_no() throws Exception {
        CustomerCreateRequest request = baseCreateRequest("중복사업자");
        willThrow(new BusinessException(CustomerErrorCode.DUPLICATE_BIZ_REG_NO))
                .given(customerService).create(any());

        mockMvc.perform(post("/api/v1/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("고객사 수정 성공")
    void update_success() throws Exception {
        // given
        CustomerUpdateRequest request = baseUpdateRequest("이름변경");

        // when & then
        mockMvc.perform(put("/api/v1/customers/{id}", 7L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNoContent());
        verify(customerService).update(eq(7L), any());
    }

    @Test
    @DisplayName("존재하지 않는 고객사 수정 시 404")
    void update_fail_not_found() throws Exception {
        CustomerUpdateRequest request = baseUpdateRequest("이름");
        willThrow(new BusinessException(CustomerErrorCode.CUSTOMER_NOT_FOUND))
                .given(customerService).update(eq(99L), any());

        mockMvc.perform(put("/api/v1/customers/{id}", 99L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("고객사 삭제 성공")
    void delete_success() throws Exception {
        mockMvc.perform(delete("/api/v1/customers/{id}", 7L))
                .andExpect(status().isNoContent());
        verify(customerService).delete(7L);
    }

    @Test
    @DisplayName("존재하지 않는 고객사 삭제 시 404")
    void delete_fail_not_found() throws Exception {
        willThrow(new BusinessException(CustomerErrorCode.CUSTOMER_NOT_FOUND))
                .given(customerService).delete(99L);

        mockMvc.perform(delete("/api/v1/customers/{id}", 99L))
                .andExpect(status().isNotFound());
    }

    private CustomerCreateRequest baseCreateRequest(String name) {
        return new CustomerCreateRequest(
                null, name, null, null, null, null,
                null, null, null, null, null, null,
                null, null, null,
                CustomerType.GENERAL, CustomerStatus.ACTIVE, null, null
        );
    }

    private CustomerUpdateRequest baseUpdateRequest(String name) {
        return new CustomerUpdateRequest(
                name, null, null, null, null,
                null, null, null, null, null, null,
                null, null, null,
                CustomerType.GENERAL, CustomerStatus.ACTIVE, null, null
        );
    }
}
