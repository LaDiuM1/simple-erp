package io.github.ladium1.erp.coderule.internal.service;

import io.github.ladium1.erp.coderule.internal.exception.CodeRuleErrorCode;
import io.github.ladium1.erp.coderule.internal.service.PatternCompiler.RenderContext;
import io.github.ladium1.erp.global.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PatternCompilerTest {

    private PatternCompiler compiler;

    private final LocalDate FIXED_DATE = LocalDate.of(2026, 4, 26);

    @BeforeEach
    void setUp() {
        compiler = new PatternCompiler();
    }

    @Test
    @DisplayName("패턴 검증 — 유효한 토큰 조합 통과")
    void validate_pattern_success() {
        compiler.validate("{PREFIX}{SEQ:3}", 3);
        compiler.validate("{PREFIX}-{YYYY}-{SEQ:4}", 4);
        compiler.validate("{YY}{MM}{DD}-{SEQ:5}", 5);
        compiler.validate("{PARENT}-{SEQ:3}", 3);
    }

    @Test
    @DisplayName("패턴 검증 — 알 수 없는 토큰은 INVALID_PATTERN")
    void validate_pattern_fail_unknown_token() {
        assertThatThrownBy(() -> compiler.validate("{UNKNOWN}-{SEQ:3}", 3))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", CodeRuleErrorCode.INVALID_PATTERN);
    }

    @Test
    @DisplayName("패턴 검증 — SEQ 토큰이 없으면 INVALID_PATTERN")
    void validate_pattern_fail_no_seq_token() {
        assertThatThrownBy(() -> compiler.validate("{PREFIX}-{YYYY}", 3))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", CodeRuleErrorCode.INVALID_PATTERN);
    }

    @Test
    @DisplayName("패턴 검증 — SEQ 외의 토큰에 자릿수 인자가 있으면 INVALID_PATTERN")
    void validate_pattern_fail_arg_on_non_seq_token() {
        assertThatThrownBy(() -> compiler.validate("{YYYY:4}-{SEQ:3}", 3))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", CodeRuleErrorCode.INVALID_PATTERN);
    }

    @Test
    @DisplayName("패턴 검증 — SEQ 자릿수가 1~18 범위 밖이면 INVALID_PATTERN")
    void validate_pattern_fail_seq_length_out_of_range() {
        assertThatThrownBy(() -> compiler.validate("{SEQ:0}", 3))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", CodeRuleErrorCode.INVALID_PATTERN);
        assertThatThrownBy(() -> compiler.validate("{SEQ:19}", 3))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", CodeRuleErrorCode.INVALID_PATTERN);
    }

    @Test
    @DisplayName("패턴 검증 — 빈 패턴은 INVALID_PATTERN")
    void validate_pattern_fail_blank() {
        assertThatThrownBy(() -> compiler.validate("", 3))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", CodeRuleErrorCode.INVALID_PATTERN);
    }

    @Test
    @DisplayName("패턴 검증 — defaultSeqLength 가 1~18 범위 밖이면 INVALID_PATTERN")
    void validate_pattern_fail_default_length_out_of_range() {
        assertThatThrownBy(() -> compiler.validate("{SEQ}", 0))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", CodeRuleErrorCode.INVALID_PATTERN);
        assertThatThrownBy(() -> compiler.validate("{SEQ}", 19))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", CodeRuleErrorCode.INVALID_PATTERN);
    }

    @Test
    @DisplayName("렌더 — PREFIX + SEQ:n 단순 패턴")
    void render_prefix_and_seq() {
        String code = compiler.render("{PREFIX}{SEQ:3}", new RenderContext("D", 4L, 3, null, FIXED_DATE));
        assertThat(code).isEqualTo("D004");
    }

    @Test
    @DisplayName("렌더 — 연도/월/일 토큰 치환")
    void render_date_tokens() {
        assertThat(compiler.render("{YYYY}", new RenderContext(null, 1L, 3, null, FIXED_DATE))).isEqualTo("2026");
        assertThat(compiler.render("{YY}", new RenderContext(null, 1L, 3, null, FIXED_DATE))).isEqualTo("26");
        assertThat(compiler.render("{MM}", new RenderContext(null, 1L, 3, null, FIXED_DATE))).isEqualTo("04");
        assertThat(compiler.render("{DD}", new RenderContext(null, 1L, 3, null, FIXED_DATE))).isEqualTo("26");
    }

    @Test
    @DisplayName("렌더 — SEQ 토큰은 자릿수 인자 우선, 없으면 defaultSeqLength")
    void render_seq_padding() {
        assertThat(compiler.render("{SEQ:5}", new RenderContext(null, 7L, 3, null, FIXED_DATE))).isEqualTo("00007");
        assertThat(compiler.render("{SEQ}", new RenderContext(null, 7L, 4, null, FIXED_DATE))).isEqualTo("0007");
    }

    @Test
    @DisplayName("렌더 — PARENT 토큰은 컨텍스트 parentCode 로 치환")
    void render_parent_token() {
        String code = compiler.render("{PARENT}-{SEQ:3}", new RenderContext(null, 2L, 3, "D001", FIXED_DATE));
        assertThat(code).isEqualTo("D001-002");
    }

    @Test
    @DisplayName("렌더 — PARENT 토큰 사용 시 parentCode 미컨텍스트면 MISSING_PARENT_CONTEXT")
    void render_fail_missing_parent_context() {
        assertThatThrownBy(() ->
                compiler.render("{PARENT}-{SEQ:3}", new RenderContext(null, 1L, 3, null, FIXED_DATE)))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", CodeRuleErrorCode.MISSING_PARENT_CONTEXT);
    }

    @Test
    @DisplayName("렌더 — PREFIX 가 null 이면 빈 문자열로 치환")
    void render_null_prefix() {
        String code = compiler.render("{PREFIX}{SEQ:3}", new RenderContext(null, 1L, 3, null, FIXED_DATE));
        assertThat(code).isEqualTo("001");
    }

    @Test
    @DisplayName("렌더 — 일자별 패턴 ({PREFIX}{YY}{MM}{DD}-{SEQ:3})")
    void render_daily_pattern() {
        String code = compiler.render("{PREFIX}{YY}{MM}{DD}-{SEQ:3}",
                new RenderContext("D", 5L, 3, null, FIXED_DATE));
        assertThat(code).isEqualTo("D260426-005");
    }

    @Test
    @DisplayName("매칭 — 패턴에 부합하는 코드는 true")
    void matches_valid_code() {
        assertThat(compiler.matches("{PREFIX}{SEQ:3}", "D", "D004")).isTrue();
        assertThat(compiler.matches("{PREFIX}-{YYYY}-{SEQ:4}", "D", "D-2026-0042")).isTrue();
    }

    @Test
    @DisplayName("매칭 — prefix 가 다른 코드는 false")
    void matches_different_prefix() {
        assertThat(compiler.matches("{PREFIX}{SEQ:3}", "D", "X004")).isFalse();
    }

    @Test
    @DisplayName("매칭 — SEQ 자릿수가 다른 코드는 false")
    void matches_wrong_seq_length() {
        assertThat(compiler.matches("{PREFIX}{SEQ:3}", "D", "D04")).isFalse();
        assertThat(compiler.matches("{PREFIX}{SEQ:3}", "D", "D0004")).isFalse();
    }

    @Test
    @DisplayName("매칭 — null 코드는 false")
    void matches_null_code() {
        assertThat(compiler.matches("{PREFIX}{SEQ:3}", "D", null)).isFalse();
    }

    @Test
    @DisplayName("PARENT 토큰 사용 여부 판별")
    void uses_parent_token() {
        assertThat(compiler.usesParentToken("{PARENT}-{SEQ:3}")).isTrue();
        assertThat(compiler.usesParentToken("{PREFIX}{SEQ:3}")).isFalse();
    }
}
