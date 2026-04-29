package io.github.ladium1.erp.coderule.internal.web;

import io.github.ladium1.erp.coderule.api.CodeRuleTarget;
import io.github.ladium1.erp.coderule.api.InputMode;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
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

    @Test
    @DisplayName("채번 규칙 목록 조회 성공")
    void find_all_success() throws Exception {
        given(codeRuleService.findAll()).willReturn(List.of(sampleResponse()));

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
        given(codeRuleService.get(CodeRuleTarget.DEPARTMENT)).willReturn(sampleResponse());

        mockMvc.perform(get("/api/v1/code-rules/{target}", "DEPARTMENT"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.target").value("DEPARTMENT"))
                .andExpect(jsonPath("$.data.pattern").value("D{SEQ:3}"));
    }

    @Test
    @DisplayName("채번 규칙 조회 — RULE_NOT_FOUND 시 404")
    void get_fail_not_found() throws Exception {
        willThrow(new BusinessException(CodeRuleErrorCode.RULE_NOT_FOUND))
                .given(codeRuleService).get(CodeRuleTarget.DEPARTMENT);

        mockMvc.perform(get("/api/v1/code-rules/{target}", "DEPARTMENT"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("채번 규칙 수정 성공")
    void update_success() throws Exception {
        CodeRuleUpdateRequest request = new CodeRuleUpdateRequest(
                "EMP-{YYYY}-{SEQ:4}", InputMode.AUTO_OR_MANUAL, "수정", null
        );
        given(codeRuleService.update(eq(CodeRuleTarget.DEPARTMENT), any())).willReturn(sampleResponse());

        mockMvc.perform(put("/api/v1/code-rules/{target}", "DEPARTMENT")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.target").value("DEPARTMENT"));
    }

    @Test
    @DisplayName("잘못된 패턴으로 수정 시 400")
    void update_fail_invalid_pattern() throws Exception {
        CodeRuleUpdateRequest request = new CodeRuleUpdateRequest(
                "{BAD}{SEQ:3}", InputMode.AUTO, null, null
        );
        willThrow(new BusinessException(CodeRuleErrorCode.INVALID_PATTERN))
                .given(codeRuleService).update(eq(CodeRuleTarget.DEPARTMENT), any());

        mockMvc.perform(put("/api/v1/code-rules/{target}", "DEPARTMENT")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("미리보기 성공 — nextCode + samples 반환")
    void preview_success() throws Exception {
        CodeRulePreviewRequest request = new CodeRulePreviewRequest(
                "D{SEQ:3}", InputMode.AUTO, null, null, null
        );
        CodeRulePreviewResponse response = CodeRulePreviewResponse.builder()
                .nextCode("D004")
                .samples(List.of("D001", "D002", "D003", "D004", "D005"))
                .build();
        given(codeRuleService.previewFromRequest(eq(CodeRuleTarget.DEPARTMENT), any()))
                .willReturn(response);

        mockMvc.perform(post("/api/v1/code-rules/{target}/preview", "DEPARTMENT")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.nextCode").value("D004"))
                .andExpect(jsonPath("$.data.samples", org.hamcrest.Matchers.hasSize(5)));
    }
}
