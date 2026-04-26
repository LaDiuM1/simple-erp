package io.github.ladium1.erp.position.internal.web;

import io.github.ladium1.erp.global.exception.BusinessException;
import io.github.ladium1.erp.global.security.MenuPermissionEvaluator;
import io.github.ladium1.erp.global.web.PageResponse;
import io.github.ladium1.erp.position.api.dto.PositionInfo;
import io.github.ladium1.erp.position.internal.dto.PositionCreateRequest;
import io.github.ladium1.erp.position.internal.dto.PositionDetailResponse;
import io.github.ladium1.erp.position.internal.dto.PositionRankingRequest;
import io.github.ladium1.erp.position.internal.dto.PositionSummaryResponse;
import io.github.ladium1.erp.position.internal.dto.PositionUpdateRequest;
import io.github.ladium1.erp.position.internal.exception.PositionErrorCode;
import io.github.ladium1.erp.position.internal.service.PositionService;
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

@WebMvcTest(PositionController.class)
@AutoConfigureMockMvc(addFilters = false)
class PositionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private PositionService positionService;

    @MockitoBean
    private MenuPermissionEvaluator menuPermissionEvaluator;

    @BeforeEach
    void allow_all_permissions() {
        given(menuPermissionEvaluator.canRead(any(), any())).willReturn(true);
        given(menuPermissionEvaluator.canWrite(any(), any())).willReturn(true);
    }

    @Test
    @DisplayName("직책 reference 목록 조회 성공")
    void find_all_success() throws Exception {
        // given
        PositionInfo info = PositionInfo.builder().id(1L).code("P001").name("이사").build();
        given(positionService.findAll()).willReturn(List.of(info));

        // when & then
        mockMvc.perform(get("/api/v1/positions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].code").value("P001"))
                .andExpect(jsonPath("$.data[0].name").value("이사"));
    }

    @Test
    @DisplayName("직책 목록 검색 성공")
    void search_success() throws Exception {
        // given
        PositionSummaryResponse summary = PositionSummaryResponse.builder()
                .id(1L).code("P001").name("이사").rankLevel(1).description(null).build();
        PageResponse<PositionSummaryResponse> page = new PageResponse<>(
                List.of(summary), 0, 20, 1, 1, false
        );
        given(positionService.search(any(), any())).willReturn(page);

        // when & then
        mockMvc.perform(get("/api/v1/positions/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].code").value("P001"))
                .andExpect(jsonPath("$.data.totalElements").value(1));
    }

    @Test
    @DisplayName("서열 순 직책 목록 조회 성공")
    void find_all_by_ranking_success() throws Exception {
        // given
        PositionSummaryResponse s1 = PositionSummaryResponse.builder()
                .id(1L).code("P001").name("이사").rankLevel(1).build();
        PositionSummaryResponse s2 = PositionSummaryResponse.builder()
                .id(2L).code("P002").name("부장").rankLevel(2).build();
        given(positionService.findAllByRanking()).willReturn(List.of(s1, s2));

        // when & then
        mockMvc.perform(get("/api/v1/positions/ranking"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].rankLevel").value(1))
                .andExpect(jsonPath("$.data[1].rankLevel").value(2));
    }

    @Test
    @DisplayName("코드 사용 가능 여부 조회 성공")
    void check_code_availability_success() throws Exception {
        given(positionService.isCodeAvailable("P999")).willReturn(true);

        mockMvc.perform(get("/api/v1/positions/code-availability").param("code", "P999"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.available").value(true));
    }

    @Test
    @DisplayName("직책 상세 조회 성공")
    void get_detail_success() throws Exception {
        // given
        PositionDetailResponse detail = PositionDetailResponse.builder()
                .id(7L).code("P007").name("팀장").rankLevel(4).description("팀 리더").build();
        given(positionService.getDetail(7L)).willReturn(detail);

        // when & then
        mockMvc.perform(get("/api/v1/positions/{id}", 7L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(7))
                .andExpect(jsonPath("$.data.name").value("팀장"))
                .andExpect(jsonPath("$.data.rankLevel").value(4));
    }

    @Test
    @DisplayName("존재하지 않는 직책 조회 시 404")
    void get_detail_fail_not_found() throws Exception {
        given(positionService.getDetail(99L))
                .willThrow(new BusinessException(PositionErrorCode.POSITION_NOT_FOUND));

        mockMvc.perform(get("/api/v1/positions/{id}", 99L))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("직책 등록 성공")
    void create_success() throws Exception {
        // given
        PositionCreateRequest request = new PositionCreateRequest(null, "신규직책", "설명");
        given(positionService.create(any())).willReturn(42L);

        // when & then
        mockMvc.perform(post("/api/v1/positions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(42));
    }

    @Test
    @DisplayName("중복 코드로 등록 시 409")
    void create_fail_duplicate_code() throws Exception {
        // given
        PositionCreateRequest request = new PositionCreateRequest("P001", "중복", null);
        willThrow(new BusinessException(PositionErrorCode.DUPLICATE_CODE))
                .given(positionService).create(any());

        // when & then
        mockMvc.perform(post("/api/v1/positions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("직책 수정 성공")
    void update_success() throws Exception {
        // given
        PositionUpdateRequest request = new PositionUpdateRequest("이름변경", "설명변경");

        // when & then
        mockMvc.perform(put("/api/v1/positions/{id}", 7L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNoContent());
        verify(positionService).update(eq(7L), any());
    }

    @Test
    @DisplayName("존재하지 않는 직책 수정 시 404")
    void update_fail_not_found() throws Exception {
        // given
        PositionUpdateRequest request = new PositionUpdateRequest("이름", null);
        willThrow(new BusinessException(PositionErrorCode.POSITION_NOT_FOUND))
                .given(positionService).update(eq(99L), any());

        // when & then
        mockMvc.perform(put("/api/v1/positions/{id}", 99L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("서열 일괄 재배치 성공")
    void reorder_success() throws Exception {
        // given
        PositionRankingRequest request = new PositionRankingRequest(List.of(2L, 1L, 3L));

        // when & then
        mockMvc.perform(put("/api/v1/positions/ranking")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNoContent());
        verify(positionService).reorder(any());
    }

    @Test
    @DisplayName("서열 재배치 페이로드 불일치 시 400")
    void reorder_fail_invalid_payload() throws Exception {
        // given
        PositionRankingRequest request = new PositionRankingRequest(List.of(1L, 2L));
        willThrow(new BusinessException(PositionErrorCode.INVALID_RANKING_PAYLOAD))
                .given(positionService).reorder(any());

        // when & then
        mockMvc.perform(put("/api/v1/positions/ranking")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("직책 삭제 성공")
    void delete_success() throws Exception {
        mockMvc.perform(delete("/api/v1/positions/{id}", 7L))
                .andExpect(status().isNoContent());
        verify(positionService).delete(7L);
    }

    @Test
    @DisplayName("존재하지 않는 직책 삭제 시 404")
    void delete_fail_not_found() throws Exception {
        // given
        willThrow(new BusinessException(PositionErrorCode.POSITION_NOT_FOUND))
                .given(positionService).delete(99L);

        // when & then
        mockMvc.perform(delete("/api/v1/positions/{id}", 99L))
                .andExpect(status().isNotFound());
    }
}
