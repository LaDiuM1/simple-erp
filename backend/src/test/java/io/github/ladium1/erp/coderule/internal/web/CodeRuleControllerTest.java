package io.github.ladium1.erp.coderule.internal.web;

import io.github.ladium1.erp.coderule.api.CodeRuleTarget;
import io.github.ladium1.erp.coderule.api.InputMode;
import io.github.ladium1.erp.coderule.api.ResetPolicy;
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

    @Test
    @DisplayName("채번 규칙 목록 조회 성공")
    void find_all_success() throws Exception {
        // given
        CodeRuleResponse response = CodeRuleResponse.builder()
                .id(1L)
                .target(CodeRuleTarget.DEPARTMENT)
                .targetLabel("부서 코드")
                .prefix("D")
                .pattern("{PREFIX}{SEQ:3}")
                .defaultSeqLength(3)
                .resetPolicy(ResetPolicy.NEVER)
                .inputMode(InputMode.AUTO)
                .parentScoped(false)
                .description("부서")
                .nextCode("D001")
                .build();
        given(codeRuleService.findAll()).willReturn(List.of(response));

        // when & then
        mockMvc.perform(get("/api/v1/code-rules"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].target").value("DEPARTMENT"))
                .andExpect(jsonPath("$.data[0].pattern").value("{PREFIX}{SEQ:3}"))
                .andExpect(jsonPath("$.data[0].nextCode").value("D001"));
    }

    @Test
    @DisplayName("채번 규칙 단건 조회 성공")
    void get_success() throws Exception {
        // given
        CodeRuleResponse response = CodeRuleResponse.builder()
                .id(1L)
                .target(CodeRuleTarget.DEPARTMENT)
                .targetLabel("부서 코드")
                .prefix("D")
                .pattern("{PREFIX}{SEQ:3}")
                .defaultSeqLength(3)
                .resetPolicy(ResetPolicy.NEVER)
                .inputMode(InputMode.AUTO)
                .parentScoped(false)
                .nextCode("D001")
                .build();
        given(codeRuleService.get(CodeRuleTarget.DEPARTMENT)).willReturn(response);

        // when & then
        mockMvc.perform(get("/api/v1/code-rules/{target}", "DEPARTMENT"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.target").value("DEPARTMENT"))
                .andExpect(jsonPath("$.data.prefix").value("D"));
    }

    @Test
    @DisplayName("존재하지 않는 채번 규칙 조회 시 404")
    void get_fail_not_found() throws Exception {
        // given
        given(codeRuleService.get(CodeRuleTarget.DEPARTMENT))
                .willThrow(new BusinessException(CodeRuleErrorCode.RULE_NOT_FOUND));

        // when & then
        mockMvc.perform(get("/api/v1/code-rules/{target}", "DEPARTMENT"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("채번 규칙 수정 성공")
    void update_success() throws Exception {
        // given
        CodeRuleUpdateRequest request = new CodeRuleUpdateRequest(
                "EMP", "{PREFIX}-{YYYY}-{SEQ:4}", 4,
                ResetPolicy.YEARLY, InputMode.AUTO_OR_MANUAL, false, "수정"
        );
        CodeRuleResponse response = CodeRuleResponse.builder()
                .id(1L)
                .target(CodeRuleTarget.DEPARTMENT)
                .targetLabel("부서 코드")
                .prefix("EMP")
                .pattern("{PREFIX}-{YYYY}-{SEQ:4}")
                .defaultSeqLength(4)
                .resetPolicy(ResetPolicy.YEARLY)
                .inputMode(InputMode.AUTO_OR_MANUAL)
                .parentScoped(false)
                .nextCode("EMP-2026-0001")
                .build();
        given(codeRuleService.update(eq(CodeRuleTarget.DEPARTMENT), any())).willReturn(response);

        // when & then
        mockMvc.perform(put("/api/v1/code-rules/{target}", "DEPARTMENT")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.prefix").value("EMP"))
                .andExpect(jsonPath("$.data.pattern").value("{PREFIX}-{YYYY}-{SEQ:4}"));
    }

    @Test
    @DisplayName("잘못된 패턴으로 수정 시 400")
    void update_fail_invalid_pattern() throws Exception {
        // given
        CodeRuleUpdateRequest request = new CodeRuleUpdateRequest(
                "D", "{BAD}{SEQ:3}", 3,
                ResetPolicy.NEVER, InputMode.AUTO, false, null
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
                "D", "{PREFIX}{SEQ:3}", 3,
                ResetPolicy.NEVER, InputMode.AUTO, false, null
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
}
