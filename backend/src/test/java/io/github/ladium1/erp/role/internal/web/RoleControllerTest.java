package io.github.ladium1.erp.role.internal.web;

import io.github.ladium1.erp.global.exception.BusinessException;
import io.github.ladium1.erp.global.menu.Menu;
import io.github.ladium1.erp.global.security.MenuPermissionEvaluator;
import io.github.ladium1.erp.global.web.PageResponse;
import io.github.ladium1.erp.role.api.dto.MenuPermission;
import io.github.ladium1.erp.role.api.dto.RoleInfo;
import io.github.ladium1.erp.role.internal.dto.MenuPermissionRequest;
import io.github.ladium1.erp.role.internal.dto.RoleCreateRequest;
import io.github.ladium1.erp.role.internal.dto.RoleDetailResponse;
import io.github.ladium1.erp.role.internal.dto.RoleSummaryResponse;
import io.github.ladium1.erp.role.internal.dto.RoleUpdateRequest;
import io.github.ladium1.erp.role.internal.exception.RoleErrorCode;
import io.github.ladium1.erp.role.internal.service.RoleService;
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

@WebMvcTest(RoleController.class)
@AutoConfigureMockMvc(addFilters = false)
class RoleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private RoleService roleService;

    @MockitoBean
    private MenuPermissionEvaluator menuPermissionEvaluator;

    @BeforeEach
    void allow_all_permissions() {
        given(menuPermissionEvaluator.canRead(any(), any())).willReturn(true);
        given(menuPermissionEvaluator.canWrite(any(), any())).willReturn(true);
    }


    @Test
    @DisplayName("권한 reference 목록 조회 성공")
    void find_all_success() throws Exception {
        RoleInfo info = RoleInfo.builder().id(1L).code("MASTER").name("관리자").system(true).build();
        given(roleService.findAll()).willReturn(List.of(info));

        mockMvc.perform(get("/api/v1/roles"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].code").value("MASTER"))
                .andExpect(jsonPath("$.data[0].system").value(true));
    }

    @Test
    @DisplayName("권한 목록 검색 성공")
    void search_success() throws Exception {
        RoleSummaryResponse summary = RoleSummaryResponse.builder()
                .id(1L).code("STAFF").name("사원").system(false).build();
        PageResponse<RoleSummaryResponse> page = new PageResponse<>(
                List.of(summary), 0, 20, 1, 1, false
        );
        given(roleService.search(any(), any())).willReturn(page);

        mockMvc.perform(get("/api/v1/roles/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].code").value("STAFF"));
    }

    @Test
    @DisplayName("코드 사용 가능 여부 조회 성공")
    void check_code_availability_success() throws Exception {
        given(roleService.isCodeAvailable("NEW")).willReturn(true);

        mockMvc.perform(get("/api/v1/roles/code-availability").param("code", "NEW"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.available").value(true));
    }

    @Test
    @DisplayName("권한 상세 조회 성공 — 매트릭스 포함")
    void get_detail_success() throws Exception {
        RoleDetailResponse detail = RoleDetailResponse.builder()
                .id(7L).code("MANAGER").name("매니저").system(false)
                .menuPermissions(List.of(new MenuPermission(Menu.EMPLOYEES, true, false)))
                .build();
        given(roleService.getDetail(7L)).willReturn(detail);

        mockMvc.perform(get("/api/v1/roles/{id}", 7L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(7))
                .andExpect(jsonPath("$.data.menuPermissions[0].menuCode").value("EMPLOYEES"))
                .andExpect(jsonPath("$.data.menuPermissions[0].canRead").value(true));
    }

    @Test
    @DisplayName("존재하지 않는 권한 조회 시 404")
    void get_detail_fail_not_found() throws Exception {
        given(roleService.getDetail(99L))
                .willThrow(new BusinessException(RoleErrorCode.ROLE_NOT_FOUND));

        mockMvc.perform(get("/api/v1/roles/{id}", 99L))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("권한 등록 성공")
    void create_success() throws Exception {
        RoleCreateRequest request = new RoleCreateRequest(
                "MANAGER", "매니저", "팀장",
                List.of(new MenuPermissionRequest(Menu.EMPLOYEES, true, true))
        );
        given(roleService.create(any())).willReturn(42L);

        mockMvc.perform(post("/api/v1/roles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(42));
    }

    @Test
    @DisplayName("중복 코드 등록 시 409")
    void create_fail_duplicate_code() throws Exception {
        RoleCreateRequest request = new RoleCreateRequest(
                "MASTER", "관리자", null,
                List.of()
        );
        willThrow(new BusinessException(RoleErrorCode.DUPLICATE_ROLE_CODE))
                .given(roleService).create(any());

        mockMvc.perform(post("/api/v1/roles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("권한 수정 성공")
    void update_success() throws Exception {
        RoleUpdateRequest request = new RoleUpdateRequest(
                "이름변경", "설명변경",
                List.of(new MenuPermissionRequest(Menu.EMPLOYEES, true, false))
        );

        mockMvc.perform(put("/api/v1/roles/{id}", 7L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNoContent());
        verify(roleService).update(eq(7L), any());
    }

    @Test
    @DisplayName("존재하지 않는 권한 수정 시 404")
    void update_fail_not_found() throws Exception {
        RoleUpdateRequest request = new RoleUpdateRequest("이름", null, List.of());
        willThrow(new BusinessException(RoleErrorCode.ROLE_NOT_FOUND))
                .given(roleService).update(eq(99L), any());

        mockMvc.perform(put("/api/v1/roles/{id}", 99L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("권한 삭제 성공")
    void delete_success() throws Exception {
        mockMvc.perform(delete("/api/v1/roles/{id}", 7L))
                .andExpect(status().isNoContent());
        verify(roleService).delete(7L);
    }

    @Test
    @DisplayName("시스템 권한 삭제 시 400")
    void delete_fail_system_role() throws Exception {
        willThrow(new BusinessException(RoleErrorCode.SYSTEM_ROLE_PROTECTED))
                .given(roleService).delete(1L);

        mockMvc.perform(delete("/api/v1/roles/{id}", 1L))
                .andExpect(status().isBadRequest());
    }
}
