package io.github.ladium1.erp.coderule.internal.web;

import io.github.ladium1.erp.coderule.api.CodeRuleTarget;
import io.github.ladium1.erp.coderule.api.InputMode;
import io.github.ladium1.erp.coderule.internal.dto.CodeRuleAttributeMappingPayload;
import io.github.ladium1.erp.coderule.internal.dto.CodeRulePreviewRequest;
import io.github.ladium1.erp.coderule.internal.dto.CodeRulePreviewResponse;
import io.github.ladium1.erp.coderule.internal.dto.CodeRuleResponse;
import io.github.ladium1.erp.coderule.internal.dto.CodeRuleUpdateRequest;
import io.github.ladium1.erp.coderule.internal.exception.CodeRuleErrorCode;
import io.github.ladium1.erp.coderule.internal.service.CodeRuleService;
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
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.IntStream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CodeRuleController.class)
@AutoConfigureMockMvc(addFilters = false)
class CodeRuleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CodeRuleService codeRuleService;

    @MockitoBean
    private MenuPermissionEvaluator menuPermissionEvaluator;

    @BeforeEach
    void allow_all_permissions() {
        given(menuPermissionEvaluator.canRead(any(), any())).willReturn(true);
        given(menuPermissionEvaluator.canWrite(any(), any())).willReturn(true);
    }

    @Test
    @DisplayName("채번 규칙 목록 조회 성공")
    void find_all_success() throws Exception {
        // given
        given(codeRuleService.findAll()).willReturn(List.of(sampleResponse()));

        // when & then
        mockMvc.perform(get("/api/v1/code-rules"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].target").value("DEPARTMENT"))
                .andExpect(jsonPath("$.data[0].pattern").value("D{SEQ:3}"))
                .andExpect(jsonPath("$.data[0].nextCode").value("D001"));
    }

    @Test
    @DisplayName("채번 규칙 단건 조회 성공")
    void get_success() throws Exception {
        // given
        given(codeRuleService.get(CodeRuleTarget.DEPARTMENT)).willReturn(sampleResponse());

        // when & then
        mockMvc.perform(get("/api/v1/code-rules/{target}", "DEPARTMENT"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.target").value("DEPARTMENT"))
                .andExpect(jsonPath("$.data.pattern").value("D{SEQ:3}"));
    }

    @Test
    @DisplayName("채번 규칙 조회 시 RULE_NOT_FOUND 면 404")
    void get_fail_not_found() throws Exception {
        // given
        willThrow(new BusinessException(CodeRuleErrorCode.RULE_NOT_FOUND))
                .given(codeRuleService).get(CodeRuleTarget.DEPARTMENT);

        // when & then
        mockMvc.perform(get("/api/v1/code-rules/{target}", "DEPARTMENT"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("채번 규칙 수정 성공")
    void update_success() throws Exception {
        // given
        CodeRuleUpdateRequest request = new CodeRuleUpdateRequest(
                "EMP-{YYYY}-{SEQ:4}", InputMode.AUTO_OR_MANUAL, "수정", null
        );
        given(codeRuleService.update(eq(CodeRuleTarget.DEPARTMENT), any())).willReturn(sampleResponse());

        // when & then
        mockMvc.perform(put("/api/v1/code-rules/{target}", "DEPARTMENT")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.target").value("DEPARTMENT"));
    }

    @Test
    @DisplayName("잘못된 패턴으로 수정 시 400")
    void update_fail_invalid_pattern() throws Exception {
        // given
        CodeRuleUpdateRequest request = new CodeRuleUpdateRequest(
                "{BAD}{SEQ:3}", InputMode.AUTO, null, null
        );
        willThrow(new BusinessException(CodeRuleErrorCode.INVALID_PATTERN))
                .given(codeRuleService).update(eq(CodeRuleTarget.DEPARTMENT), any());

        // when & then
        mockMvc.perform(put("/api/v1/code-rules/{target}", "DEPARTMENT")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("미리보기 성공 — nextCode + samples 반환")
    void preview_success() throws Exception {
        // given
        CodeRulePreviewRequest request = new CodeRulePreviewRequest(
                "D{SEQ:3}", InputMode.AUTO, null, null, null
        );
        CodeRulePreviewResponse response = CodeRulePreviewResponse.builder()
                .nextCode("D004")
                .samples(List.of("D001", "D002", "D003", "D004", "D005"))
                .build();
        given(codeRuleService.previewFromRequest(eq(CodeRuleTarget.DEPARTMENT), any()))
                .willReturn(response);

        // when & then
        mockMvc.perform(post("/api/v1/code-rules/{target}/preview", "DEPARTMENT")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.nextCode").value("D004"))
                .andExpect(jsonPath("$.data.samples", org.hamcrest.Matchers.hasSize(5)));
    }

    @Test
    @DisplayName("미리보기 map/list는 정확히 20개까지 허용")
    void preview_collection_exact_boundary_is_allowed() throws Exception {
        Map<String, String> attributes = new LinkedHashMap<>();
        IntStream.range(0, 20).forEach(index -> attributes.put("key-" + index, "value-" + index));
        List<CodeRuleAttributeMappingPayload> mappings = IntStream.range(0, 20)
                .mapToObj(index -> new CodeRuleAttributeMappingPayload(
                        "key-" + index, "value-" + index, "C" + index
                ))
                .toList();
        CodeRulePreviewRequest request = new CodeRulePreviewRequest(
                "D{SEQ:3}", InputMode.AUTO, "PARENT", attributes, mappings
        );
        given(codeRuleService.previewFromRequest(eq(CodeRuleTarget.DEPARTMENT), any()))
                .willReturn(CodeRulePreviewResponse.builder()
                        .nextCode("D004")
                        .samples(List.of("D004"))
                        .build());

        mockMvc.perform(post("/api/v1/code-rules/{target}/preview", "DEPARTMENT")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("미리보기 map/list 21개는 service 진입 전에 거부")
    void preview_collection_overflow_is_rejected() throws Exception {
        Map<String, String> attributes = new LinkedHashMap<>();
        IntStream.range(0, 21).forEach(index -> attributes.put("key-" + index, "value-" + index));
        CodeRulePreviewRequest mapOverflow = new CodeRulePreviewRequest(
                "D{SEQ:3}", InputMode.AUTO, null, attributes, null
        );
        List<CodeRuleAttributeMappingPayload> mappings = IntStream.range(0, 21)
                .mapToObj(index -> new CodeRuleAttributeMappingPayload(
                        "key-" + index, "value-" + index, "C" + index
                ))
                .toList();
        CodeRulePreviewRequest listOverflow = new CodeRulePreviewRequest(
                "D{SEQ:3}", InputMode.AUTO, null, null, mappings
        );

        for (CodeRulePreviewRequest request : List.of(mapOverflow, listOverflow)) {
            mockMvc.perform(post("/api/v1/code-rules/{target}/preview", "DEPARTMENT")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }
        verify(codeRuleService, never()).previewFromRequest(any(), any());
    }

    @Test
    @DisplayName("미리보기 map key/value·parent 길이와 null list 원소를 제한")
    void preview_nested_values_are_bounded() throws Exception {
        List<CodeRulePreviewRequest> invalidRequests = List.of(
                new CodeRulePreviewRequest(
                        "D{SEQ:3}", InputMode.AUTO, "P".repeat(101), null, null
                ),
                new CodeRulePreviewRequest(
                        "D{SEQ:3}", InputMode.AUTO, null, Map.of("K".repeat(51), "value"), null
                ),
                new CodeRulePreviewRequest(
                        "D{SEQ:3}", InputMode.AUTO, null, Map.of("key", "V".repeat(101)), null
                ),
                new CodeRulePreviewRequest(
                        "D{SEQ:3}", InputMode.AUTO, null,
                        java.util.Collections.singletonMap("key", null), null
                ),
                new CodeRulePreviewRequest(
                        "D{SEQ:3}", InputMode.AUTO, null, Map.of("", "value"), null
                ),
                new CodeRulePreviewRequest(
                        "D{SEQ:3}", InputMode.AUTO, null, null,
                        java.util.Collections.singletonList(null)
                )
        );

        for (CodeRulePreviewRequest request : invalidRequests) {
            mockMvc.perform(post("/api/v1/code-rules/{target}/preview", "DEPARTMENT")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }
        verify(codeRuleService, never()).previewFromRequest(any(), any());
    }

    @Test
    @DisplayName("수정 매핑은 정확히 20개까지 허용")
    void update_mapping_exact_boundary_is_allowed() throws Exception {
        List<CodeRuleAttributeMappingPayload> mappings = IntStream.range(0, 20)
                .mapToObj(index -> new CodeRuleAttributeMappingPayload(
                        "key-" + index, "value-" + index, "C" + index
                ))
                .toList();
        CodeRuleUpdateRequest request = new CodeRuleUpdateRequest(
                "D{SEQ:3}", InputMode.AUTO, null, mappings
        );
        given(codeRuleService.update(eq(CodeRuleTarget.DEPARTMENT), any()))
                .willReturn(sampleResponse());

        mockMvc.perform(put("/api/v1/code-rules/{target}", "DEPARTMENT")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("수정 매핑 21개는 service 진입 전에 거부")
    void update_mapping_overflow_is_rejected() throws Exception {
        List<CodeRuleAttributeMappingPayload> mappings = IntStream.range(0, 21)
                .mapToObj(index -> new CodeRuleAttributeMappingPayload(
                        "key-" + index, "value-" + index, "C" + index
                ))
                .toList();
        CodeRuleUpdateRequest request = new CodeRuleUpdateRequest(
                "D{SEQ:3}", InputMode.AUTO, null, mappings
        );

        mockMvc.perform(put("/api/v1/code-rules/{target}", "DEPARTMENT")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
        verify(codeRuleService, never()).update(any(), any());
    }

    private static CodeRuleResponse sampleResponse() {
        return CodeRuleResponse.builder()
                .id(1L)
                .target(CodeRuleTarget.DEPARTMENT)
                .targetLabel("부서 코드")
                .pattern("D{SEQ:3}")
                .inputMode(InputMode.AUTO)
                .hasParent(true)
                .description("부서")
                .nextCode("D001")
                .build();
    }
}
