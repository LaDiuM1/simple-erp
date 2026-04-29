package io.github.ladium1.erp.coderule.internal.service;

import io.github.ladium1.erp.coderule.api.CodeRuleAttributeProvider;
import io.github.ladium1.erp.coderule.api.CodeRuleTarget;
import io.github.ladium1.erp.coderule.api.InputMode;
import io.github.ladium1.erp.coderule.api.dto.CodeGenerationContext;
import io.github.ladium1.erp.coderule.api.dto.CodeRuleInfo;
import io.github.ladium1.erp.coderule.internal.dto.CodeRulePreviewRequest;
import io.github.ladium1.erp.coderule.internal.dto.CodeRulePreviewResponse;
import io.github.ladium1.erp.coderule.internal.dto.CodeRuleResponse;
import io.github.ladium1.erp.coderule.internal.dto.CodeRuleUpdateRequest;
import io.github.ladium1.erp.coderule.internal.entity.CodeRule;
import io.github.ladium1.erp.coderule.internal.entity.CodeSequence;
import io.github.ladium1.erp.coderule.internal.exception.CodeRuleErrorCode;
import io.github.ladium1.erp.coderule.internal.mapper.CodeRuleMapper;
import io.github.ladium1.erp.coderule.internal.repository.CodeRuleAttributeMappingRepository;
import io.github.ladium1.erp.coderule.internal.repository.CodeRuleRepository;
import io.github.ladium1.erp.coderule.internal.repository.CodeSequenceRepository;
import io.github.ladium1.erp.global.exception.BusinessException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CodeRuleServiceTest {

    @Mock private CodeRuleRepository codeRuleRepository;
    @Mock private CodeSequenceRepository codeSequenceRepository;
    @Mock private CodeRuleAttributeMappingRepository attributeMappingRepository;
    @Mock private CodeRuleMapper codeRuleMapper;
    @Mock private PatternCompiler patternCompiler;
    @Mock private ScopeKeyResolver scopeKeyResolver;

    private CodeRuleService codeRuleService;

    private static final CodeRuleTarget TARGET = CodeRuleTarget.DEPARTMENT;
    private static final String SCOPE_KEY = "GLOBAL";

    private void initWithoutAttributes() {
        codeRuleService = new CodeRuleService(
                codeRuleRepository,
                codeSequenceRepository,
                attributeMappingRepository,
                codeRuleMapper,
                patternCompiler,
                scopeKeyResolver,
                List.<CodeRuleAttributeProvider>of()
        );
    }

    @Test
    @DisplayName("getRule 성공")
    void get_rule_success() {
        initWithoutAttributes();
        CodeRule rule = mockRule();
        CodeRuleInfo info = CodeRuleInfo.builder().target(TARGET).pattern("D{SEQ:3}").inputMode(InputMode.AUTO).build();
        given(codeRuleRepository.findByTarget(TARGET)).willReturn(Optional.of(rule));
        given(codeRuleMapper.toInfo(rule)).willReturn(info);

        assertThat(codeRuleService.getRule(TARGET)).isEqualTo(info);
    }

    @Test
    @DisplayName("getRule 실패 — RULE_NOT_FOUND")
    void get_rule_fail_not_found() {
        initWithoutAttributes();
        given(codeRuleRepository.findByTarget(TARGET)).willReturn(Optional.empty());
        assertThatThrownBy(() -> codeRuleService.getRule(TARGET))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", CodeRuleErrorCode.RULE_NOT_FOUND);
    }

    @Test
    @DisplayName("generate — 시퀀스 row 가 없으면 신규 생성")
    void generate_new_sequence() {
        initWithoutAttributes();
        CodeRule rule = mockRule();
        given(codeRuleRepository.findByTarget(TARGET)).willReturn(Optional.of(rule));
        given(patternCompiler.usedAttributeKeys(any(), any())).willReturn(Set.of());
        given(scopeKeyResolver.resolve(any(), any(), any(), any(), any())).willReturn(SCOPE_KEY);
        given(codeSequenceRepository.findForUpdate(TARGET, SCOPE_KEY)).willReturn(Optional.empty());
        given(codeSequenceRepository.saveAndFlush(any(CodeSequence.class)))
                .willAnswer(inv -> inv.getArgument(0));
        given(patternCompiler.render(eq("D{SEQ:3}"), any())).willReturn("D001");

        String code = codeRuleService.generate(TARGET, CodeGenerationContext.empty());

        assertThat(code).isEqualTo("D001");
        verify(codeSequenceRepository).saveAndFlush(any(CodeSequence.class));
    }

    @Test
    @DisplayName("generate — 기존 시퀀스 increment")
    void generate_increments_existing() {
        initWithoutAttributes();
        CodeRule rule = mockRule();
        CodeSequence existing = CodeSequence.first(TARGET, SCOPE_KEY);
        existing.increment();

        given(codeRuleRepository.findByTarget(TARGET)).willReturn(Optional.of(rule));
        given(patternCompiler.usedAttributeKeys(any(), any())).willReturn(Set.of());
        given(scopeKeyResolver.resolve(any(), any(), any(), any(), any())).willReturn(SCOPE_KEY);
        given(codeSequenceRepository.findForUpdate(TARGET, SCOPE_KEY)).willReturn(Optional.of(existing));
        given(patternCompiler.render(eq("D{SEQ:3}"), any())).willReturn("D003");

        String code = codeRuleService.generate(TARGET, CodeGenerationContext.empty());

        assertThat(code).isEqualTo("D003");
        assertThat(existing.getCurrentSeq()).isEqualTo(3L);
        verify(codeSequenceRepository, never()).saveAndFlush(any(CodeSequence.class));
    }

    @Test
    @DisplayName("preview — 시퀀스 increment 안 함")
    void preview_no_increment() {
        initWithoutAttributes();
        CodeRule rule = mockRule();
        CodeSequence existing = CodeSequence.first(TARGET, SCOPE_KEY);

        given(codeRuleRepository.findByTarget(TARGET)).willReturn(Optional.of(rule));
        given(patternCompiler.usedAttributeKeys(any(), any())).willReturn(Set.of());
        given(scopeKeyResolver.resolve(any(), any(), any(), any(), any())).willReturn(SCOPE_KEY);
        given(codeSequenceRepository.findByTargetAndScopeKey(TARGET, SCOPE_KEY))
                .willReturn(Optional.of(existing));
        given(patternCompiler.render(eq("D{SEQ:3}"), any())).willReturn("D002");

        String code = codeRuleService.preview(TARGET, CodeGenerationContext.empty());

        assertThat(code).isEqualTo("D002");
        assertThat(existing.getCurrentSeq()).isEqualTo(1L);
    }

    @Test
    @DisplayName("validate — 패턴 일치 시 통과")
    void validate_passes() {
        initWithoutAttributes();
        CodeRule rule = mockRule();
        given(codeRuleRepository.findByTarget(TARGET)).willReturn(Optional.of(rule));
        given(patternCompiler.matches("D{SEQ:3}", "D004")).willReturn(true);
        codeRuleService.validate(TARGET, "D004");
    }

    @Test
    @DisplayName("validate — 패턴 불일치 시 CODE_FORMAT_MISMATCH")
    void validate_fail_mismatch() {
        initWithoutAttributes();
        CodeRule rule = mockRule();
        given(codeRuleRepository.findByTarget(TARGET)).willReturn(Optional.of(rule));
        given(patternCompiler.matches("D{SEQ:3}", "X-bad")).willReturn(false);

        assertThatThrownBy(() -> codeRuleService.validate(TARGET, "X-bad"))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", CodeRuleErrorCode.CODE_FORMAT_MISMATCH);
    }

    @Test
    @DisplayName("findAll — CodeRuleResponse 리스트 반환")
    void find_all() {
        initWithoutAttributes();
        CodeRule rule = mockRule();
        given(codeRuleRepository.findAll()).willReturn(List.of(rule));
        given(patternCompiler.usedAttributeKeys(any(), any())).willReturn(Set.of());
        given(patternCompiler.usesParentToken("D{SEQ:3}")).willReturn(false);
        given(scopeKeyResolver.resolve(any(), any(), any(), any(), any())).willReturn(SCOPE_KEY);
        given(codeSequenceRepository.findByTargetAndScopeKey(TARGET, SCOPE_KEY))
                .willReturn(Optional.empty());
        given(patternCompiler.render(eq("D{SEQ:3}"), any())).willReturn("D001");

        List<CodeRuleResponse> result = codeRuleService.findAll();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).target()).isEqualTo(TARGET);
        assertThat(result.get(0).nextCode()).isEqualTo("D001");
    }

    @Test
    @DisplayName("update — 패턴 검증 통과 후 엔티티 update")
    void update_success() {
        initWithoutAttributes();
        CodeRule rule = mockRule();
        CodeRuleUpdateRequest request = new CodeRuleUpdateRequest(
                "EMP-{YYYY}-{SEQ:4}", InputMode.AUTO, "수정", null
        );
        given(codeRuleRepository.findByTarget(TARGET)).willReturn(Optional.of(rule));
        given(patternCompiler.usedAttributeKeys(any(), any())).willReturn(Set.of());
        given(patternCompiler.usesParentToken(any())).willReturn(false);
        given(scopeKeyResolver.resolve(any(), any(), any(), any(), any())).willReturn(SCOPE_KEY);
        given(codeSequenceRepository.findByTargetAndScopeKey(TARGET, SCOPE_KEY))
                .willReturn(Optional.empty());
        given(patternCompiler.render(any(), any())).willReturn("EMP-2026-0001");

        CodeRuleResponse response = codeRuleService.update(TARGET, request);

        verify(patternCompiler).validate(eq("EMP-{YYYY}-{SEQ:4}"), any());
        assertThat(rule.getPattern()).isEqualTo("EMP-{YYYY}-{SEQ:4}");
        assertThat(response.target()).isEqualTo(TARGET);
    }

    @Test
    @DisplayName("update — 잘못된 패턴이면 INVALID_PATTERN 전파")
    void update_fail_invalid_pattern() {
        initWithoutAttributes();
        CodeRuleUpdateRequest request = new CodeRuleUpdateRequest(
                "{BAD}{SEQ:3}", InputMode.AUTO, null, null
        );
        willThrow(new BusinessException(CodeRuleErrorCode.INVALID_PATTERN))
                .given(patternCompiler).validate(eq("{BAD}{SEQ:3}"), any());

        assertThatThrownBy(() -> codeRuleService.update(TARGET, request))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", CodeRuleErrorCode.INVALID_PATTERN);
    }

    @Test
    @DisplayName("previewFromRequest — nextCode + samples 5건 반환")
    void preview_from_request() {
        initWithoutAttributes();
        CodeRulePreviewRequest request = new CodeRulePreviewRequest(
                "D{SEQ:3}", InputMode.AUTO, null, null, null
        );
        given(patternCompiler.usedAttributeKeys(any(), any())).willReturn(Set.of());
        given(scopeKeyResolver.resolve(any(), any(), any(), any(), any())).willReturn(SCOPE_KEY);
        given(codeSequenceRepository.findByTargetAndScopeKey(TARGET, SCOPE_KEY))
                .willReturn(Optional.empty());
        given(patternCompiler.render(eq("D{SEQ:3}"), any())).willReturn("D-RENDERED");

        CodeRulePreviewResponse response = codeRuleService.previewFromRequest(TARGET, request);

        verify(patternCompiler).validate(eq("D{SEQ:3}"), any());
        assertThat(response.nextCode()).isEqualTo("D-RENDERED");
        assertThat(response.samples()).hasSize(5);
    }

    private CodeRule mockRule() {
        return CodeRule.builder()
                .target(TARGET)
                .pattern("D{SEQ:3}")
                .inputMode(InputMode.AUTO)
                .description("테스트")
                .build();
    }
}
