package io.github.ladium1.erp.afterservice.internal.web;

import io.github.ladium1.erp.afterservice.internal.dto.EngineerRequest;
import io.github.ladium1.erp.afterservice.internal.dto.EngineerResponse;
import io.github.ladium1.erp.afterservice.internal.entity.EngineerType;
import io.github.ladium1.erp.afterservice.internal.exception.AfterServiceErrorCode;
import io.github.ladium1.erp.afterservice.internal.service.EngineerService;
import io.github.ladium1.erp.global.exception.BusinessException;
import io.github.ladium1.erp.global.security.MenuPermissionEvaluator;
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

@WebMvcTest(EngineerController.class)
@AutoConfigureMockMvc(addFilters = false)
class EngineerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private EngineerService engineerService;

    @MockitoBean
    private MenuPermissionEvaluator menuPermissionEvaluator;

    @BeforeEach
    void allow_all_permissions() {
        given(menuPermissionEvaluator.canRead(any(), any())).willReturn(true);
        given(menuPermissionEvaluator.canWrite(any(), any())).willReturn(true);
    }

    @Test
    @DisplayName("엔지니어 목록 조회 성공")
    void find_all_success() throws Exception {
        // given
        EngineerResponse engineer = EngineerResponse.builder()
                .id(5L).name("김기사").type(EngineerType.OUTSOURCED).affiliation("문영테크").active(true)
                .build();
        given(engineerService.findAll()).willReturn(List.of(engineer));

        // when & then
        mockMvc.perform(get("/api/v1/after-services/engineers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].name").value("김기사"))
                .andExpect(jsonPath("$.data[0].affiliation").value("문영테크"));
    }

    @Test
    @DisplayName("엔지니어 등록 성공")
    void create_success() throws Exception {
        // given
        EngineerRequest request = new EngineerRequest("김기사", EngineerType.OUTSOURCED, "문영테크", null, null, true);
        given(engineerService.create(any())).willReturn(5L);

        // when & then
        mockMvc.perform(post("/api/v1/after-services/engineers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(5));
    }

    @Test
    @DisplayName("이름 미입력 등록 시 400")
    void create_fail_missing_name() throws Exception {
        // given
        EngineerRequest request = new EngineerRequest(" ", EngineerType.OUTSOURCED, null, null, null, true);

        // when & then
        mockMvc.perform(post("/api/v1/after-services/engineers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("엔지니어 수정 성공")
    void update_success() throws Exception {
        // given
        EngineerRequest request = new EngineerRequest("김기사", EngineerType.OUTSOURCED, "금광이엔지", null, null, true);

        // when & then
        mockMvc.perform(put("/api/v1/after-services/engineers/{id}", 5L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNoContent());
        verify(engineerService).update(eq(5L), any());
    }

    @Test
    @DisplayName("참조 중 엔지니어 삭제 시 400")
    void delete_fail_in_use() throws Exception {
        // given
        willThrow(new BusinessException(AfterServiceErrorCode.ENGINEER_IN_USE))
                .given(engineerService).delete(5L);

        // when & then
        mockMvc.perform(delete("/api/v1/after-services/engineers/{id}", 5L))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("엔지니어 삭제 성공")
    void delete_success() throws Exception {
        mockMvc.perform(delete("/api/v1/after-services/engineers/{id}", 5L))
                .andExpect(status().isNoContent());
        verify(engineerService).delete(5L);
    }
}
