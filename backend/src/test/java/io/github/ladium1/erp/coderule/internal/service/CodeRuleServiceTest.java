package io.github.ladium1.erp.coderule.internal.service;

import io.github.ladium1.erp.coderule.api.CodeRuleTarget;
import io.github.ladium1.erp.coderule.api.InputMode;
import io.github.ladium1.erp.coderule.api.ResetPolicy;
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
import io.github.ladium1.erp.coderule.internal.repository.CodeRuleRepository;
import io.github.ladium1.erp.coderule.internal.repository.CodeSequenceRepository;
import io.github.ladium1.erp.global.exception.BusinessException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

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

    @InjectMocks
    private CodeRuleService codeRuleService;

    @Mock private CodeRuleRepository codeRuleRepository;
    @Mock private CodeSequenceRepository codeSequenceRepository;
    @Mock private CodeRuleMapper codeRuleMapper;
    @Mock private PatternCompiler patternCompiler;
    @Mock private ScopeKeyResolver scopeKeyResolver;

    private static final CodeRuleTarget TARGET = CodeRuleTarget.DEPARTMENT;
    private static final String SCOPE_KEY = "GLOBAL";

    @Test
    @DisplayName("getRule 성공 — Mapper 가 변환한 Info 반환")
    void get_rule_success() {
        // given
        CodeRule rule = mockRule();
        CodeRuleInfo info = CodeRuleInfo.builder().target(TARGET).prefix("D").pattern("{PREFIX}{SEQ:3}").build();
        given(codeRuleRepository.findByTarget(TARGET)).willReturn(Optional.of(rule));
        given(codeRuleMapper.toInfo(rule)).willReturn(info);

        // when
        CodeRuleInfo actual = codeRuleService.getRule(TARGET);

        // then
        assertThat(actual).isEqualTo(info);
    }

    @Test
    @DisplayName("getRule 실패 — RULE_NOT_FOUND")
    void get_rule_fail_not_found() {
        // given
        given(codeRuleRepository.findByTarget(TARGET)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> codeRuleService.getRule(TARGET))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", CodeRuleErrorCode.RULE_NOT_FOUND);
    }

    @Test
    @DisplayName("generate — 시퀀스 row 가 없으면 신규 생성하고 1 반환")
    void generate_creates_new_sequence_when_absent() {
        // given
        CodeRule rule = mockRule();
        given(codeRuleRepository.findByTarget(TARGET)).willReturn(Optional.of(rule));
        given(scopeKeyResolver.resolve(eq(rule), any(), any())).willReturn(SCOPE_KEY);
        given(codeSequenceRepository.findForUpdate(TARGET, SCOPE_KEY)).willReturn(Optional.empty());
        given(codeSequenceRepository.saveAndFlush(any(CodeSequence.class)))
                .willAnswer(inv -> inv.getArgument(0));
        given(patternCompiler.render(eq("{PREFIX}{SEQ:3}"), any())).willReturn("D001");

        // when
        String code = codeRuleService.generate(TARGET, CodeGenerationContext.empty());

        // then
        assertThat(code).isEqualTo("D001");
        verify(codeSequenceRepository).saveAndFlush(any(CodeSequence.class));
    }

    @Test
    @DisplayName("generate — 기존 시퀀스 row 가 있으면 increment 후 반환")
    void generate_increments_existing_sequence() {
        // given
        CodeRule rule = mockRule();
        CodeSequence existing = CodeSequence.first(TARGET, SCOPE_KEY); // currentSeq=1
        existing.increment(); // currentSeq=2
        existing.increment(); // currentSeq=3 — 다음 발급 시 4 가 되어야 함

        given(codeRuleRepository.findByTarget(TARGET)).willReturn(Optional.of(rule));
        given(scopeKeyResolver.resolve(eq(rule), any(), any())).willReturn(SCOPE_KEY);
        given(codeSequenceRepository.findForUpdate(TARGET, SCOPE_KEY)).willReturn(Optional.of(existing));
        given(patternCompiler.render(eq("{PREFIX}{SEQ:3}"), any())).willReturn("D004");

        // when
        String code = codeRuleService.generate(TARGET, CodeGenerationContext.empty());

        // then
        assertThat(code).isEqualTo("D004");
        assertThat(existing.getCurrentSeq()).isEqualTo(4L);
        verify(codeSequenceRepository, never()).saveAndFlush(any(CodeSequence.class));
    }

    @Test
    @DisplayName("preview — 기존 시퀀스 +1 로 렌더하고 시퀀스는 증가시키지 않음")
    void preview_returns_next_code_without_increment() {
        // given
        CodeRule rule = mockRule();
        CodeSequence existing = CodeSequence.first(TARGET, SCOPE_KEY); // currentSeq=1

        given(codeRuleRepository.findByTarget(TARGET)).willReturn(Optional.of(rule));
        given(scopeKeyResolver.resolve(eq(rule), any(), any())).willReturn(SCOPE_KEY);
        given(codeSequenceRepository.findByTargetAndScopeKey(TARGET, SCOPE_KEY))
                .willReturn(Optional.of(existing));
        given(patternCompiler.render(eq("{PREFIX}{SEQ:3}"), any())).willReturn("D002");

        // when
        String code = codeRuleService.preview(TARGET, CodeGenerationContext.empty());

        // then
        assertThat(code).isEqualTo("D002");
        assertThat(existing.getCurrentSeq()).isEqualTo(1L); // unchanged
    }

    @Test
    @DisplayName("preview — 시퀀스 row 가 없으면 1 로 시작")
    void preview_returns_1_when_no_sequence() {
        // given
        CodeRule rule = mockRule();
        given(codeRuleRepository.findByTarget(TARGET)).willReturn(Optional.of(rule));
        given(scopeKeyResolver.resolve(eq(rule), any(), any())).willReturn(SCOPE_KEY);
        given(codeSequenceRepository.findByTargetAndScopeKey(TARGET, SCOPE_KEY))
                .willReturn(Optional.empty());
        given(patternCompiler.render(eq("{PREFIX}{SEQ:3}"), any())).willReturn("D001");

        // when
        String code = codeRuleService.preview(TARGET, CodeGenerationContext.empty());

        // then
        assertThat(code).isEqualTo("D001");
    }

    @Test
    @DisplayName("validate — 패턴에 부합하면 통과")
    void validate_passes_when_matches() {
        // given
        CodeRule rule = mockRule();
        given(codeRuleRepository.findByTarget(TARGET)).willReturn(Optional.of(rule));
        given(patternCompiler.matches("{PREFIX}{SEQ:3}", "D", "D004")).willReturn(true);

        // when & then — no exception
        codeRuleService.validate(TARGET, "D004");
    }

    @Test
    @DisplayName("validate — 패턴 불일치 시 CODE_FORMAT_MISMATCH")
    void validate_fail_when_mismatch() {
        // given
        CodeRule rule = mockRule();
        given(codeRuleRepository.findByTarget(TARGET)).willReturn(Optional.of(rule));
        given(patternCompiler.matches("{PREFIX}{SEQ:3}", "D", "X-bad")).willReturn(false);

        // when & then
        assertThatThrownBy(() -> codeRuleService.validate(TARGET, "X-bad"))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", CodeRuleErrorCode.CODE_FORMAT_MISMATCH);
    }

    @Test
    @DisplayName("findAll — 모든 규칙을 CodeRuleResponse 로 반환")
    void find_all_returns_all_rules() {
        // given
        CodeRule rule = mockRule();
        given(codeRuleRepository.findAll()).willReturn(List.of(rule));
        given(scopeKeyResolver.resolve(eq(rule), any(), any())).willReturn(SCOPE_KEY);
        given(codeSequenceRepository.findByTargetAndScopeKey(TARGET, SCOPE_KEY))
                .willReturn(Optional.empty());
        given(patternCompiler.usesParentToken("{PREFIX}{SEQ:3}")).willReturn(false);
        given(patternCompiler.render(eq("{PREFIX}{SEQ:3}"), any())).willReturn("D001");

        // when
        List<CodeRuleResponse> result = codeRuleService.findAll();

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).target()).isEqualTo(TARGET);
        assertThat(result.get(0).nextCode()).isEqualTo("D001");
    }

    @Test
    @DisplayName("update — 패턴 검증 통과 후 엔티티 update 호출")
    void update_success() {
        // given
        CodeRule rule = mockRule();
        CodeRuleUpdateRequest request = new CodeRuleUpdateRequest(
                "EMP", "{PREFIX}-{YYYY}-{SEQ:4}", 4,
                ResetPolicy.YEARLY, InputMode.AUTO, false, "수정"
        );
        given(codeRuleRepository.findByTarget(TARGET)).willReturn(Optional.of(rule));
        given(scopeKeyResolver.resolve(eq(rule), any(), any())).willReturn(SCOPE_KEY);
        given(codeSequenceRepository.findByTargetAndScopeKey(TARGET, SCOPE_KEY))
                .willReturn(Optional.empty());
        given(patternCompiler.usesParentToken(any())).willReturn(false);
        given(patternCompiler.render(any(), any())).willReturn("EMP-2026-0001");

        // when
        CodeRuleResponse response = codeRuleService.update(TARGET, request);

        // then
        verify(patternCompiler).validate("{PREFIX}-{YYYY}-{SEQ:4}", 4);
        assertThat(rule.getPrefix()).isEqualTo("EMP");
        assertThat(rule.getPattern()).isEqualTo("{PREFIX}-{YYYY}-{SEQ:4}");
        assertThat(response.target()).isEqualTo(TARGET);
    }

    @Test
    @DisplayName("update — 잘못된 패턴이면 PatternCompiler 가 throw 한 INVALID_PATTERN 전파")
    void update_fail_invalid_pattern() {
        // given
        CodeRuleUpdateRequest request = new CodeRuleUpdateRequest(
                "D", "{BAD}{SEQ:3}", 3,
                ResetPolicy.NEVER, InputMode.AUTO, false, null
        );
        willThrow(new BusinessException(CodeRuleErrorCode.INVALID_PATTERN))
                .given(patternCompiler).validate("{BAD}{SEQ:3}", 3);

        // when & then
        assertThatThrownBy(() -> codeRuleService.update(TARGET, request))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", CodeRuleErrorCode.INVALID_PATTERN);
    }

    @Test
    @DisplayName("previewFromRequest — 시뮬레이션 샘플 5건 + nextCode 반환")
    void preview_from_request_returns_samples() {
        // given
        CodeRulePreviewRequest request = new CodeRulePreviewRequest(
                "D", "{PREFIX}{SEQ:3}", 3,
                ResetPolicy.NEVER, InputMode.AUTO, false, null
        );
        given(scopeKeyResolver.resolve(ResetPolicy.NEVER, false, null, java.time.LocalDate.now()))
                .willReturn(SCOPE_KEY);
        given(codeSequenceRepository.findByTargetAndScopeKey(TARGET, SCOPE_KEY))
                .willReturn(Optional.empty());
        given(patternCompiler.render(eq("{PREFIX}{SEQ:3}"), any())).willReturn("D-RENDERED");

        // when
        CodeRulePreviewResponse response = codeRuleService.previewFromRequest(TARGET, request);

        // then
        verify(patternCompiler).validate("{PREFIX}{SEQ:3}", 3);
        assertThat(response.nextCode()).isEqualTo("D-RENDERED");
        assertThat(response.samples()).hasSize(5);
    }

    private CodeRule mockRule() {
        return CodeRule.builder()
                .target(TARGET)
                .prefix("D")
                .pattern("{PREFIX}{SEQ:3}")
                .defaultSeqLength(3)
                .resetPolicy(ResetPolicy.NEVER)
                .inputMode(InputMode.AUTO)
                .parentScoped(false)
                .description("테스트")
                .build();
    }
}
